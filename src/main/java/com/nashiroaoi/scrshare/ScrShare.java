package com.nashiroaoi.scrshare;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Mod(ScrShare.MOD_ID)
public class ScrShare {
    public static final String MOD_ID = "scrshare";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final HttpClient httpclient = HttpClients.createDefault();
    public static Minecraft mc = Minecraft.getInstance();

    public ScrShare() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);

    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    @SubscribeEvent
    public void onScreenShot(ScreenshotEvent event) throws IOException {
        byte[] data = event.getImage().getBytes();
        String base64str = Base64.getEncoder().encodeToString(data);
        imgurPoster(base64str);
    }

    private void imgurPoster(String base64str) throws IOException {
        HttpPost httppost = new HttpPost("https://api.imgur.com/3/image");
        httppost.setHeader("Authorization", "Client-ID aa5c2230d959d2e");
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("image", base64str));
        params.add(new BasicNameValuePair("type", "base64"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse responsePost = httpclient.execute(httppost);
        BufferedReader reader = new BufferedReader(new InputStreamReader(responsePost.getEntity().getContent()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String getResponseString = sb.toString();
        Model model = new Gson().fromJson(getResponseString, Model.class);
        if(model.success){
            String url = model.data.link;
            LOGGER.info(url);
        }
        else {
            LOGGER.info(model.status);
        }

    }
}
class Data{
    @SerializedName("link")
    @Expose
    public String link;

}

class Model {

    @SerializedName("data")
    @Expose
    public Data data;
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("status")
    @Expose
    public Integer status;
}