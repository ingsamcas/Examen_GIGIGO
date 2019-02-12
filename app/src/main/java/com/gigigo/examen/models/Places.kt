package com.gigigo.examen.models


import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import org.json.JSONObject
import java.util.ArrayList


class Places(val context:Context) {
    data class Place(val name: String,val photo:String,val icon: String,val place_id: String,
                     val vicinity: String,val latitud:Double,val longitud:Double,
                     var allPhotos:ArrayList<String>,var allComents:ArrayList<Review>,var rate:Float):Serializable
    data class Review(val influencer:String,val rate:Float,val comment:String):Serializable
    companion object {
        @JvmField val Places = "Places"
        @JvmField val RESULT_OK = 1
        @JvmField val RESULT_ERROR = 0
        @JvmField val Maps_Endpoint1 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=19.4325987,-99.1419372&radius=1000&types=food&key=[apikey]"
        @JvmField val Maps_Endpoint2 = "https://maps.googleapis.com/maps/api/place/details/json?placeid=[placeid]&key=[apikey]"
    }
    var placesList: ArrayList<Place> = ArrayList<Place>()

    fun getPlaces(listener: (ArrayList<Place>) -> Unit) {

        if(placesList.size==0){
            val apiKey=context.packageManager.getApplicationInfo("com.gigigo.examen",PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY")

            downloadPlaces(Maps_Endpoint1.replace("[apikey]",apiKey,true),
                    object:ResultReceiver(Handler()){
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            val json = JSONObject(resultData?.get(Places) as String)
                            val jsonResponseArray = json.getJSONArray("results")
                            val photoURL="https://maps.googleapis.com/maps/api/place/photo?maxwidth=100&photoreference=[photo_reference]&key="+apiKey;
                            for(i in 0..jsonResponseArray.length()-1){
                                var photo_reference=jsonResponseArray.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getString("photo_reference")

                                placesList.add(Place(jsonResponseArray.getJSONObject(i).getString("name"),
                                        photoURL.replace("[photo_reference]",photo_reference),
                                        jsonResponseArray.getJSONObject(i).getString("icon"),
                                        jsonResponseArray.getJSONObject(i).getString("place_id"),
                                        jsonResponseArray.getJSONObject(i).getString("vicinity"),
                                        jsonResponseArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                                        jsonResponseArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng"),
                                        ArrayList<String>(),ArrayList<Review>(),0.0f))//Dummy data, it will be filled with correct data when the user wants to read the detail


                            }
                            listener.invoke(placesList)
                        }
                    } )
        }else{
            listener.invoke(placesList)
        }

    }

    fun getDetailForPlace(place: Place,listener: (Place) -> Unit){
        val apiKey=context.packageManager.getApplicationInfo("com.gigigo.examen",PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY")
        downloadPlaces(Maps_Endpoint2.replace("[placeid]",place.place_id).replace("[apikey]",apiKey),
                object:ResultReceiver(Handler()){
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {

                        val json = JSONObject(resultData?.get(Places) as String)
                        val jsonResponse = json.getJSONObject("result")

                        //Get photos references(200px size)
                        val photosjsonArray=jsonResponse.getJSONArray("photos")
                        val photoURL="https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference=[photo_reference]&key="+apiKey;
                        for(i in 0..photosjsonArray.length()-1){
                            var photo_reference=photosjsonArray.getJSONObject(i).getString("photo_reference")
                            place?.allPhotos!!.add(photoURL.replace("[photo_reference]",photo_reference))
                        }

                        //Get Comments
                        if(jsonResponse.has("reviews")){
                            val comentsjsonArray=jsonResponse.getJSONArray("reviews")
                            for(i in 0..comentsjsonArray.length()-1){
                                val review=Review(comentsjsonArray.getJSONObject(i).getString("author_name"),
                                        comentsjsonArray.getJSONObject(i).getDouble("rating").toFloat(),
                                        comentsjsonArray.getJSONObject(i).getString("text"))
                                place?.allComents!!.add(review)
                            }
                        }


                        //Get Rate
                       if(jsonResponse.has("rating")) place?.rate= jsonResponse.getDouble("rating").toFloat()

                        listener.invoke(place)
                    }
                })
    }
    private fun downloadPlaces(endPoint: String, receiver: ResultReceiver) {

        val queryTask = PlacesTask(receiver)
        queryTask.execute(endPoint)


    }

    private class PlacesTask(val receiver:ResultReceiver): AsyncTask<String, String, String>() {


        override  fun onPreExecute() {
            super.onPreExecute()

        }

        override fun doInBackground(vararg params:String):String {


            var connection:HttpURLConnection?=null
            var reader:BufferedReader?=null

            try {
                var url:URL = URL(params[0])

                connection = url.openConnection() as HttpURLConnection
                connection.connect()


                var stream: InputStream  = connection.getInputStream()
                reader = BufferedReader( InputStreamReader(stream))

                var buffer: StringBuffer  = StringBuffer()
                var line:String? = ""

                do{
                    try{
                        line = reader.readLine()
                        buffer.append(line+"\n")
                    }catch (e:Exception){
                        line=null
                    }

                } while (line != null)

                return buffer.toString();


            } catch (e:MalformedURLException) {
                e.printStackTrace();
                receiver.send(RESULT_ERROR,null)
            } catch (e:IOException) {
                e.printStackTrace();
                receiver.send(RESULT_ERROR,null)
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
                try {
                    if (reader != null) {
                        reader.close()
                    }
                } catch (e:IOException) {
                    e.printStackTrace()
                    receiver.send(RESULT_ERROR,null)
                }
            }
            return "";
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var bundle: Bundle=Bundle()
            bundle.putString(Places,result)
            receiver.send(RESULT_OK,bundle)

        }
    }


}


