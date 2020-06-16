package uni.fvv.zgvideo;

import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniZGVideoMap {

   public static Map<String,UniZGVideoComponent> componentList = new HashMap<>();

   public static View getComponentView(String name){
       UniZGVideoComponent uniZGVideoComponent = componentList.get(name);
       if(uniZGVideoComponent == null){
           return null;
       }
       return  uniZGVideoComponent.getHostView();
   }

}
