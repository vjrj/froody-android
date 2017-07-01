# AdminApi

All URIs are relative to *https://api.froody-app.at*

Method | HTTP request | Description
------------- | ------------- | -------------
[**adminCleanupGet**](AdminApi.md#adminCleanupGet) | **GET** /admin/cleanup | 
[**adminEntryDeleteGet**](AdminApi.md#adminEntryDeleteGet) | **GET** /admin/entry/delete | 


<a name="adminCleanupGet"></a>
# **adminCleanupGet**
> ResponseOk adminCleanupGet(adminCode)



Clean up user and entry database

### Example
```java
// Import classes:
//import io.github.froodyapp.api.invoker.ApiException;
//import io.github.froodyapp.api.api.AdminApi;


AdminApi apiInstance = new AdminApi();
String adminCode = "adminCode_example"; // String | AdminCode - defined in config.php
try {
    ResponseOk result = apiInstance.adminCleanupGet(adminCode);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AdminApi#adminCleanupGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **adminCode** | **String**| AdminCode - defined in config.php |

### Return type

[**ResponseOk**](ResponseOk.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="adminEntryDeleteGet"></a>
# **adminEntryDeleteGet**
> ResponseOk adminEntryDeleteGet(adminCode, entryIds)



Delete an Entry using admin privileges

### Example
```java
// Import classes:
//import io.github.froodyapp.api.invoker.ApiException;
//import io.github.froodyapp.api.api.AdminApi;


AdminApi apiInstance = new AdminApi();
String adminCode = "adminCode_example"; // String | AdminCode - defined in config.php
List<Long> entryIds = Arrays.asList(56L); // List<Long> | Array(Entry.entryId ** UID of the entry, which was added to DB)
try {
    ResponseOk result = apiInstance.adminEntryDeleteGet(adminCode, entryIds);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AdminApi#adminEntryDeleteGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **adminCode** | **String**| AdminCode - defined in config.php |
 **entryIds** | [**List&lt;Long&gt;**](Long.md)| Array(Entry.entryId ** UID of the entry, which was added to DB) |

### Return type

[**ResponseOk**](ResponseOk.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

