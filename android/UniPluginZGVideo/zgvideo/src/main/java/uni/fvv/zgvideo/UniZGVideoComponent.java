package uni.fvv.zgvideo;

import android.content.Context;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

public class UniZGVideoComponent extends WXComponent<TextureView> {

    public String name = "";

    public UniZGVideoComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected TextureView initComponentHostView(@NonNull Context context) {
        return new TextureView(context);
    }

    @WXComponentProp(name = "name")
    public void setName(String name) {
        if (name == "") {
            return;
        }
        this.name = name;
        UniZGVideoMap.componentList.put(name,this);
    }

    @JSMethod
    public void test(){
        Toast.makeText(getContext(),"test",Toast.LENGTH_SHORT).show();
    }

}
