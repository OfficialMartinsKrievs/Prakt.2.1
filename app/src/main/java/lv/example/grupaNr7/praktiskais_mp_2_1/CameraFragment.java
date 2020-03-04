package lv.example.grupaNr7.praktiskais_mp_2_1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1888;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1337;
    ImageView mImageView;
    GridView mGridView;
    String mCurrentPhotoPath;
    ArrayList<File> list;

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraFragment.
     */

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View camera = inflater.inflate(R.layout.fragment_camera, container, false);
        mGridView = camera.findViewById(R.id.GridView);
        Button btnCapture = camera.findViewById(R.id.button);
        mImageView = (ImageView) camera.findViewById(R.id.imageView);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    invokeCamera();
                } else {
                    // lets request permission
                    String[] permissinRequest = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissinRequest, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });
        return camera;
    }

    //searching for files in external memory, if found save them inside arraylist
    private ArrayList<File> imageReader(File externalStoragePublicDirectory) {
        ArrayList<File> mlist = new ArrayList<>();

        File[] files = externalStoragePublicDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                mlist.addAll(imageReader(files[i]));
            } else {
                if (files[i].getName().endsWith(".jpg")) {
                    mlist.add(files[i]);
                }
            }
        }
        return mlist;
    }

    class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.my_grid, parent, false);
            ImageView image = convertView.findViewById(R.id.imageView2);
            image.setImageURI(Uri.parse(getItem(position).toString()));

            return convertView;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // we have heard back from camera
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                invokeCamera();
            } else {
                Toast.makeText(getActivity(), getString(R.string.cannotopencamera), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void invokeCamera() {
        //get file uri
        Uri pictureUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", createImageFile());

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //tell camera where to save image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);

        //tell the camera to request right permission

        intent.setFlags(intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private File createImageFile() {
        //public picture directory
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //timestamp makes unique name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        //put together the directory and the timestamp
        File imageFile = new File(pictureDirectory, "picture" + timestamp + ".jpg");
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    if (resultCode == Activity.RESULT_OK) {
                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContext().getContentResolver(), Uri.fromFile(file));
                        if (bitmap != null) {
                            mImageView.setImageBitmap(bitmap);
                        }
                    }
                    list = imageReader(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                    mGridView.setAdapter(new GridAdapter());
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.errorDisplaying), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imageDeleter(file);

    }

    private void imageDeleter(File file) {
        if(file.exists()){
            File[] theData = file.listFiles();
            for (int i = 0; i < theData.length; i++) {
                File oneFile = theData[i];
                if (oneFile.isDirectory()) {
                } else {
                    if (oneFile.getName().endsWith(".jpg")) {
                        oneFile.delete();
                    }
                }
            }
        }

    }
}
