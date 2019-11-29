package com.driver

/**
 * Created by techintegrity on 08/10/16.
 */
object Url {

    //public static String baseUrl = "http://138.68.5.43/web_service/";
    var baseUrl = "http://digitaldwarka.com/taxiapp/web_service/"
    var driver_sign_up = baseUrl + "driver_sign_up"
    var driver_edit_profile = baseUrl + "driver_profile_edit"
    var driver_login = baseUrl + "driver_login?"
    var driver_forgot_password = baseUrl + "driver_forgot_password?"
    var DriverTripUrl = baseUrl + "driver_bookings"
    var DriverAcceptTripUrl = baseUrl + "driver_accept_trip"
    var DriverRejectTripUrl = baseUrl + "driver_reject_trip"
    var DriverFilterTripUrl = baseUrl + "driver_filter_book"

    var DriverArrivedTripUrl = baseUrl + "driver_arrived_trip"
    var DriverOnTripUrl = baseUrl + "driver_on_trip"
    var DriverCompletedTripUrl = baseUrl + "driver_completed_trip"
    var DriverChangPasswordUrl = baseUrl + "driver_change_password"
    var DriverLatLongSave = "https://lezcocabs.klifftechnologies.com/api/driver/insert.php"

    var driver_cartype = baseUrl + "car_type"
    var imageurl = "http://138.68.5.43/driverimages/"
    var userImageUrl = "http://138.68.5.43/user_image/"
    var subscribeUrl = "http://162.243.225.225:8002/subscribe"
    var unsubscribeUrl = "http://162.243.225.225:8002/unsubscribe"
    var carImageUrl = "http://138.68.5.43/car_image/"
    var FacebookImgUrl = "https://graph.facebook.com/"
}
