package sky.alien.korenovskguide.adapter;

import android.view.View;

/**
 * Created by NeoGhost on 05.04.2017.
 */

public interface ClickListenerChatFirebase {

    void clickImageChat(View view, int position,String nameUser,String urlPhotoUser,String urlPhotoClick);


    void clickImageMapChat(View view, int position,String latitude,String longitude);

}
