package newcapec.com.mycustomevview;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE=1;
    public static final int MEDIA_TYPE_VIDEO=2;
    public static final String TAG="cpf";
    public static final String SUB_PATH="cpf";

    //UI
    private Button btn;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    //DATA
    private File vFile;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mMediaPlay;

    private boolean isRecord=true;

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mediaRecorder, int i, int i1) {
            if (mediaRecorder != null) {
                mediaRecorder.reset();
            }
        }
    };

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            if (surfaceHolder.getSurface() == null) {
                return;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stopCamera();
        }
    };

    private void initCamera() {
        if (camera != null) {
            stopCamera();
        }
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (camera == null) {
            Toast.makeText(this, "未找到相机!", Toast.LENGTH_LONG);
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        btn = (Button) findViewById(R.id.button);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView2);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(callback);

        initCamera();

    }
    //点击按钮事件
    public void  btnClk(View view){
        Log.d(TAG,btn.getText().toString());
        if(isRecord){
            if(preStartRecord()){
                mediaRecorder.start();
                btn.setText("stop");
            }else{
                releaseMediaR();
                releaseCamera();
            }
        }
        if(!isRecord){
            releaseMediaR();
            stopCamera();
            btn.setText("start");
        }
        isRecord=!isRecord;
    }

    private void releaseMediaR(){
        if(mediaRecorder!=null){
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder=null;
        }

    }

    private void releaseCamera(){
        if(camera!=null){
            camera.unlock();
            camera=null;
        }
    }

    /**
     * 准备录像
     * @return
     */
    public boolean preStartRecord(){
            mediaRecorder=new MediaRecorder();
        //set up camera
        if(camera==null) {
            Log.d(TAG, " ..............Camera is null!!!!");
            camera=Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        camera.unlock();

        mediaRecorder.setCamera(camera);

        //set source
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //set CamCoderprofile
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        Log.d(TAG,getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        //set outfile
       //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        //set preview out
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

        //prepare configured media
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            return false;
        }
        return true;
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
//测试一下
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM), SUB_PATH);
//        File mediaStorageDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    //启动其他界面
    public  void btnActivity(View view){
        Intent mIntent=new Intent(this,C2Activity.class);
        startActivity(mIntent);
    }

    //得到视频存储路径
    public  String getStoreDir(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+File.separator+SUB_PATH;
    }

    //合成并预览视频文件
    public void btnMeger(View view) throws Exception {
        String fPath=this.getStoreDir();
        File dir=new File(fPath);
        if(!dir.isDirectory()){
            Log.e(TAG,fPath+" isn't  Direct!!");
            throw new Exception(fPath+" isn't  Direct!!");
        }

        //合成mp4 start
        List<Movie> moviesLst=new LinkedList<Movie>();

        for(File f:dir.listFiles()){
            String file=f.getAbsolutePath();
            //ParserUtil pu=new ParserUtil();解析视频文件
            //pu.parseVFile(f);
            Movie movie=MovieCreator.build(file);
            moviesLst.add(movie);
        }

        List<Track> vTrack=new LinkedList<>();
        List<Track> aTrack=new LinkedList<>();

        for(Movie m :moviesLst){
            for(Track t:m.getTracks()){
                if(t.getHandler().equals("soun")){
                    aTrack.add(t);
                }
                if(t.getHandler().equals("vide")){
                    vTrack.add(t);
                }
                Log.i(TAG,t.getHandler()+":t.getHandle()");
            }
        }

        Movie outMovie=new Movie();//合成文件
        //分别咎音频和视频
        if(aTrack.size()>0){
            outMovie.addTrack(new AppendTrack(aTrack.toArray(new Track[aTrack.size()])));
        }
        if(vTrack.size()>0){
            outMovie.addTrack(new AppendTrack(vTrack.toArray(new Track[vTrack.size()])));
        }

        //合成文件
        Container mp4File=new DefaultMp4Builder().build(outMovie);
        String endFile=fPath+File.separator+"test.mp4";
        FileChannel fc=new RandomAccessFile(endFile,"rw").getChannel();
        mp4File.writeContainer(fc);
        fc.close();

        Toast.makeText(this, R.string.suc,Toast.LENGTH_LONG);
    }

    /**
     * 播放文件
     * @param file
     */
    public  void initMediaPlayAndPlay(String file){
        mMediaPlay=new MediaPlayer();
        mMediaPlay.reset();
        try {
            mMediaPlay.setDataSource(file);//播放的文件
            mMediaPlay.setDisplay(surfaceView.getHolder());
            mMediaPlay.prepareAsync();
            mMediaPlay.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlay.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
