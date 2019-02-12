
  /*******************************************************************
  * Customer.java
  * Generated by Backendless Corp.
  ********************************************************************/
		package eclipseapps.mobility.parkeame.backendservice.openpay;
import eclipseapps.mobility.parkeame.backendservice.openpay.Address;
public class Customer
{
    private Address address;
    private float balance;
    private String clabe;
    private java.util.Date creationDate;
    private String email;
    private String externalId;
    private String id;
    private String lastName;
    private String name;
    private String phoneNumber;
    private Boolean requiresAccount;
    private String status;
    public void setAddress(Address address)
    {
      this.address = address;
    }
    public void setBalance(float balance)
    {
      this.balance = balance;
    }
    public void setClabe(String clabe)
    {
      this.clabe = clabe;
    }
    public void setCreationDate(java.util.Date creationDate)
    {
      this.creationDate = creationDate;
    }
    public void setEmail(String email)
    {
      this.email = email;
    }
    public void setExternalId(String externalId)
    {
      this.externalId = externalId;
    }
    public void setId(String id)
    {
      this.id = id;
    }
    public void setLastName(String lastName)
    {
      this.lastName = lastName;
    }
    public void setName(String name)
    {
      this.name = name;
    }
    public void setPhoneNumber(String phoneNumber)
    {
      this.phoneNumber = phoneNumber;
    }
    public void setRequiresAccount(Boolean requiresAccount)
    {
      this.requiresAccount = requiresAccount;
    }
    public void setStatus(String status)
    {
      this.status = status;
    }
    public Address getAddress( )
    {
      return address;
    }
    public float getBalance( )
    {
      return balance;
    }
    public String getClabe( )
    {
      return clabe;
    }
    public java.util.Date getCreationDate( )
    {
      return creationDate;
    }
    public String getEmail( )
    {
      return email;
    }
    public String getExternalId( )
    {
      return externalId;
    }
    public String getId( )
    {
      return id;
    }
    public String getLastName( )
    {
      return lastName;
    }
    public String getName( )
    {
      return name;
    }
    public String getPhoneNumber( )
    {
      return phoneNumber;
    }
    public Boolean getRequiresAccount( )
    {
      return requiresAccount;
    }
    public String getStatus( )
    {
      return status;
    }
}