package eclipseapps.mobility.parkeame.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.google.api.client.util.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import eclipseapps.android.dialogs.Dialogs;
import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.activities.Credentials;
import eclipseapps.mobility.parkeame.activities.MainActivity;
import eclipseapps.mobility.parkeame.backendservice.DemoService;
import eclipseapps.mobility.parkeame.cloud.DBParkeame;
import eclipseapps.mobility.parkeame.cloud.Saldos;
import eclipseapps.mobility.parkeame.cloud.Tokens;
import eclipseapps.payments.UI.Adapters.OnEditAccountsListeners;
import eclipseapps.payments.UI.Adapters.OnPayRequestListener;
import eclipseapps.payments.UI.Fragments.AccountManager;
import eclipseapps.payments.UI.Fragments.CreditManager;
import eclipseapps.payments.UI.dataCard;
import pay.openpay.Openpay;
import pay.openpay.OperationCallBack;
import pay.openpay.OperationResult;
import pay.openpay.exceptions.OpenpayServiceException;
import pay.openpay.exceptions.ServiceUnavailableException;
import pay.openpay.implementation.AddCardActivity;
import pay.openpay.implementation.DeviceIdFragment;
import pay.openpay.implementation.MessageDialogFragment;
import pay.openpay.implementation.OpenPayApp;
import pay.openpay.implementation.ProgressDialogFragment;
import pay.openpay.model.Card;
import pay.openpay.model.Token;
import pay.openpay.validation.CardValidator;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by usuario on 28/10/17.
 */

public class fragment_pago extends CreditManager implements OperationCallBack,AsyncCallback {
    DBParkeame DB;
    String sessionId;
    public fragment_pago(){
        super();
        DB=DBParkeame.getInstance(getActivity());
        setOnEditAccountListener(new OnEditAccountsListeners() {

            @Override
            public List<dataCard> getCards() {
                final Cursor cur=DB.getallfrom("Tokens");
                List<Tokens> tokens=DB.mapCursorToObjectList(cur,Tokens.class);
                List<dataCard> cards=new ArrayList<>();

                if(tokens.size()>0){
                    for (Tokens token:tokens) {
                        dataCard card=new dataCard();
                        card.setBrand(token.getBrand_());
                        card.setCardNumber(token.getCardNumber_());
                        card.setHolderName(token.getHolderName_());
                        card.setId(token.getToken_());
                        card.setSelected(token.getSelected_());
                        cards.add(card);
                    }
                }
                return cards;
            }

            @Override
            public void newDataCardListener(String NombreTarjeta, String NumeroTarjeta, String CVV, String Month, String Year) {
                Openpay openpay = ((OpenPayApp) (getActivity().getApplication())).getOpenpay();
                Card card = new Card();
                boolean isValid = true;
                card.holderName(NombreTarjeta);
                String Error="Ups algo va mal!!, intentalo de nuevo.";
                if (!CardValidator.validateHolderName(NombreTarjeta)) {
                    Error=getActivity().getString(com.pay.R.string.invalid_holder_name);
                    isValid = false;
                }

                card.cardNumber(NumeroTarjeta);
                if (!CardValidator.validateNumber(NumeroTarjeta)) {
                    Error=getActivity().getString(com.pay.R.string.invalid_card_number);
                    isValid = false;
                }

                card.cvv2(CVV);
                if (!CardValidator.validateCVV(CVV, NumeroTarjeta)) {
                    Error=getActivity().getString(com.pay.R.string.invalid_cvv);
                    isValid = false;
                }

                if (!CardValidator.validateExpiryDate(Integer.valueOf(Month), Integer.valueOf(Year))) {
                    Error=getActivity().getString(com.pay.R.string.invalid_expire_date);
                    isValid = false;
                }

                card.expirationMonth(Integer.valueOf(Month));
                card.expirationYear(Integer.valueOf(Year)-2000);//2018-2000=18

                if (isValid) {
                    fragment_pago.this.wait(com.pay.R.string.progress_message,true);
                    openpay.createToken(card, fragment_pago.this);
                }else{
                    DialogFragment fragment = MessageDialogFragment.newInstance(com.pay.R.string.error, Error);
                    fragment.show(getActivity().getFragmentManager(), "Error");
                }
            }

            @Override
            public boolean deleteCardListener(final String Id) {
                //0:Error 1:No encontro el token en backendless TimeOfDelete:Ok

                String response=DemoService.getInstance().deleteCustomerCard(Credentials.ActualUser.getpayId(), Id);
                if (response==null){
                    DB.getDBInstance().delete("Tokens","Token_='"+Id+"'",null);
                    return true;
                }else{
                    //Mensaje de error
                    return false;
                }

            }

            @Override
            public void setDefaultCard(String Id) {
                DB.getDBInstance().execSQL("UPDATE Tokens SET Selected_=0");
                DB.getDBInstance().execSQL("UPDATE Tokens SET Selected_=1 WHERE Token_='"+Id+"'");
            }
        });
        setOnPayListener(new OnPayRequestListener() {
            @Override
            public OrderMap getPrices() {
                OrderMap prices=new OrderMap();
                prices.put("$16.00 mxn",16.0f);
                prices.put("$32.00 mxn",32.0f);
                prices.put("$64.00 mxn",64.0f);
                prices.put("$128.00 mxn",128.0f);
                return prices;
            }

            @Override
            public void onPayRequested(final String MethodId,final String Description, final float quantity) {

                fragment_pago.this.wait("Realizando el cargo...",false);
                final Saldos saldo=new Saldos();
                saldo.setCantidad_(quantity);
                saldo.setDescripcion_("Recarga de saldo: "+Description);
                Backendless.Data.save(saldo, new AsyncCallback<Saldos>() {
                    @Override
                    public void handleResponse(Saldos response) {
                        //Creo un ObjectId en Backendless: Se utiliza como el OrderId al momento del pago.
                        sessionId=new DeviceIdFragment().getDeviceId(getActivity());
                        final String ObjectId=response.getObjectId();
                        DemoService.getInstance().addPaymentFromClientAsync(sessionId,Credentials.ActualUser.getObjectId(), Credentials.ActualUser.getpayId(), ObjectId, MethodId, quantity, new AsyncCallback<String>() {
                            @Override
                            public void handleResponse(String response) {
                                //Se debe actualizar el objeto en la nube de Backendless
                                if (response==null){
                                    //Problema al hacer el cargo
                                    return;
                                }
                                Credentials.ActualUser.setlastSessionId(sessionId);
                                Backendless.UserService.update(Credentials.ActualUser, new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser response) {
                                        DemoService.getInstance().getCustomerAsync(Credentials.ActualUser.getpayId(), new AsyncCallback<String>() {
                                            @Override
                                            public void handleResponse(String response) {
                                                if (response==null){
                                                    //Problema al hacer el cargo
                                                    return;
                                                }
                                                try {
                                                    JSONObject json = new JSONObject(response);
                                                    float saldo= (float) json.getDouble("balance");
                                                    SharedPreferences prefs = getActivity().getSharedPreferences("eclipseapps.mobility.parkeame", MODE_PRIVATE);
                                                    prefs.edit().putFloat("balance", saldo).commit();
                                                    DecimalFormat df = new DecimalFormat("#.00");
                                                    misaldo.setText("$ "+df.format(saldo));
                                                    //Felcidades:Recarga exitosa!!!
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                fragment_pago.this.dismissWait();

                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                                fragment_pago.this.dismissWait();
                                            }
                                        });
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        fragment_pago.this.dismissWait();
                                    }
                                });
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                //Erroe si no se pudo hacer el cargo
                                fragment_pago.this.dismissWait();
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        //Error si no se pudo crear la orden en backendless
                        fragment_pago.this.dismissWait();
                    }
                });

            }
        });
    }
    Token result;
    //region OPEN PAY CALLBACKS!!!!!!!!!!!!!
    @Override
    public void onError(OpenpayServiceException error) {
        AddCardActivity act=new AddCardActivity();
        act.onError(error);
        fragment_pago.this.dismissWait();
        DialogFragment fragment = MessageDialogFragment.newInstance(com.pay.R.string.error,act.getError(error));
        fragment.show(getActivity().getFragmentManager(), "Error");
    }

    @Override
    public void onCommunicationError(ServiceUnavailableException error) {
        new AddCardActivity().onCommunicationError(error);
    }
    @Override
    public void onSuccess(OperationResult operationResult) {
        result= (Token) operationResult.getResult();
        String sessionId=new DeviceIdFragment().getDeviceId(getActivity());
        DemoService.getInstance().addCardToCustomerAsync(Credentials.ActualUser.getpayId(),result.getId(),sessionId, this);
    }
    //endregion

    //region Backendless-service-OpenPay Callbacks
    @Override
    public void handleResponse(Object response) {
        String res=(String) response;
        if (res.contains("The customer with id '")&& res.contains("' does not exist")){
            DemoService.getInstance().addBasicCustomerAsync(Credentials.ActualUser.getNombre(), Credentials.ActualUser.getEmail(), true, new AsyncCallback<String>() {
                @Override
                public void handleResponse(String response) {
                    Credentials.ActualUser.setPayId(response);
                    if (Credentials.ActualUser.saveAndSync()){
                        String sessionId=new DeviceIdFragment().getDeviceId(getActivity());
                        DemoService.getInstance().addCardToCustomerAsync(Credentials.ActualUser.getpayId(),result.getId(),sessionId, fragment_pago.this);
                    }else{
                        //Mensaje de error
                    }

                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    //Mesaje de error
                }
            });
            return;
        }
        final Tokens token=new Tokens();
        token.setBrand_(result.getCard().getBrand());
        token.setCardNumber_(result.getCard().getCardNumber());
        token.setHolderName_(result.getCard().getHolderName());
        token.setToken_(res);
        DateTime time=result.getCard().getCreationDate();
        long value=time.getValue();
        token.setCreationDate_(value);
        token.setSelected_(true);
        token.savein(DB);
        fragment_pago.this.dismissWait();
        DialogFragment fragment = MessageDialogFragment.newInstance(com.pay.R.string.card_added,
                getActivity().getString(com.pay.R.string.card_created));
        fragment.show(getActivity().getFragmentManager(), getActivity().getString(com.pay.R.string.info));
        ((MainActivity)getActivity()).selectItem(2);
    }

    @Override
    public void handleFault(BackendlessFault fault) {

    }


    //endregion
}
