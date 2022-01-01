package instant.saver.for_instagram.api;

import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.model.UserInfoForSingleStoryDownload;
import instant.saver.for_instagram.model.ResponseModel;
import instant.saver.for_instagram.model.story.FullDetailModel;
import instant.saver.for_instagram.model.story.StoryModel;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface APISERVICES {
    @GET
    Observable<ResponseModel> callResult(@Url String Value, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent, @Header("X-IG-App-ID") String apId, @Header("Accept") String accept, @Header("Accept-Language") String acceptLanguage, @Header("X-ASBD-ID") String XASBDID, @Header("X-IG-WWW-Claim") String XIGWWWClaim, @Header("Origin") String origin, @Header("DNT") String dnt, @Header("Connection") String conn, @Header("Referer") String referer, @Header("Sec-Fetch-Dest") String secfecdest,
                                         @Header("Sec-Fetch-Mode") String mode, @Header("Sec-Fetch-Site") String site, @Header("Sec-GPC") String gpc, @Header("TE") String te);

    @GET
    Observable<StoryModel> getStoriesApi(@Url String Value, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent, @Header("X-IG-App-ID") String apId, @Header("Accept") String accept, @Header("Accept-Language") String acceptLanguage, @Header("X-ASBD-ID") String XASBDID, @Header("X-IG-WWW-Claim") String XIGWWWClaim, @Header("Origin") String origin, @Header("DNT") String dnt, @Header("Connection") String conn, @Header("Referer") String referer, @Header("Sec-Fetch-Dest") String secfecdest,
                                         @Header("Sec-Fetch-Mode") String mode, @Header("Sec-Fetch-Site") String site, @Header("Sec-GPC") String gpc, @Header("TE") String te);

    @GET
    Observable<FullDetailModel> getStoriesFullDetailInfoApi(@Url String Value, @Query("reel_ids") String userId, @Header("X-IG-App-ID") String apId, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent, @Header("Accept") String accept, @Header("Accept-Language") String acceptLanguage, @Header("X-ASBD-ID") String XASBDID, @Header("X-IG-WWW-Claim") String XIGWWWClaim, @Header("Origin") String origin, @Header("DNT") String dnt, @Header("Connection") String conn, @Header("Referer") String referer, @Header("Sec-Fetch-Dest") String secfecdest,
                                                            @Header("Sec-Fetch-Mode") String mode, @Header("Sec-Fetch-Site") String site, @Header("Sec-GPC") String gpc, @Header("TE") String te);

    @GET
    Observable<PhotosFeedModel> getPhotoFullDetailInfoApi(@Url String Value, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent, @Query("query_hash") String query_hash, @Query("variables") String Variables,@Header("X-IG-App-ID") String apId,@Header("Accept") String accept, @Header("Accept-Language") String acceptLanguage, @Header("X-ASBD-ID") String XASBDID, @Header("X-IG-WWW-Claim") String XIGWWWClaim, @Header("Origin") String origin, @Header("DNT") String dnt, @Header("Connection") String conn, @Header("Referer") String referer, @Header("Sec-Fetch-Dest") String secfecdest,
                                                          @Header("Sec-Fetch-Mode") String mode, @Header("Sec-Fetch-Site") String site, @Header("Sec-GPC") String gpc, @Header("TE") String te);

    @GET
    Observable<UserInfoForSingleStoryDownload> getUserIdForStoryDownload(@Url String url, @Header("Cookie") String cookies, @Header("User-Agent") String userAgent,@Header("X-IG-App-ID") String apId,@Header("Accept") String accept, @Header("Accept-Language") String acceptLanguage, @Header("X-ASBD-ID") String XASBDID, @Header("X-IG-WWW-Claim") String XIGWWWClaim, @Header("Origin") String origin, @Header("DNT") String dnt, @Header("Connection") String conn, @Header("Referer") String referer, @Header("Sec-Fetch-Dest") String secfecdest,
                                                                         @Header("Sec-Fetch-Mode") String mode, @Header("Sec-Fetch-Site") String site, @Header("Sec-GPC") String gpc, @Header("TE") String te);

    @GET
    Observable<FullDetailModel> getStoryToDownload(@Url String Value, @Query("reel_ids") String userId, @Header("X-IG-App-ID") String apId, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent, @Header("Accept") String accept, @Header("Accept-Language") String acceptLanguage, @Header("X-ASBD-ID") String XASBDID, @Header("X-IG-WWW-Claim") String XIGWWWClaim, @Header("Origin") String origin, @Header("DNT") String dnt, @Header("Connection") String conn, @Header("Referer") String referer, @Header("Sec-Fetch-Dest") String secfecdest,
                                                   @Header("Sec-Fetch-Mode") String mode, @Header("Sec-Fetch-Site") String site, @Header("Sec-GPC") String gpc, @Header("TE") String te);
}
