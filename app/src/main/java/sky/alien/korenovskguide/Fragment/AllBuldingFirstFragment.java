package sky.alien.korenovskguide.Fragment;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import sky.alien.korenovskguide.Category;
import sky.alien.korenovskguide.CategoryAdapter;
import sky.alien.korenovskguide.R;




public class AllBuldingFirstFragment extends Fragment {


    private CategoryAdapter adapter;
    private List<Category> categoryList;

    public AllBuldingFirstFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bulding_layout, container, false);


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(getContext(), categoryList);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareAlbums();
        try

        {
            Glide.with(this).load(R.drawable.picfa).into((ImageView)rootView.findViewById(R.id.backdrop));
        } catch (Exception e) {
            Log.e("Carregar", "No rutilla");
            e.printStackTrace();
        }

        return rootView;

    }




    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }




    /**
     * Adding few albums for testing
     */
    private void prepareAlbums() {
        int[] covers = new int[]{
                R.drawable.one,
                R.drawable.two,
                R.drawable.thee,
                R.drawable.four,
                R.drawable.five,
                R.drawable.six,
                R.drawable.seven,
                R.drawable.eight,
                R.drawable.nine,
                R.drawable.ten,
                R.drawable.eleneven};

        Category a = new Category("Магазины", 13, covers[0]);
        categoryList.add(a);

        a = new Category("Аптеки", 8, covers[1]);
        categoryList.add(a);

        a = new Category("Автомашины", 11, covers[2]);
        categoryList.add(a);

        a = new Category("Гос. учереждения", 12, covers[3]);
        categoryList.add(a);

        a = new Category("Цветы", 14, covers[4]);
        categoryList.add(a);

        a = new Category("Развлечения", 1, covers[5]);
        categoryList.add(a);

        a = new Category("Экстренные службы", 11, covers[6]);
        categoryList.add(a);

        a = new Category("Спорт", 14, covers[7]);
        categoryList.add(a);

        a = new Category("Образование", 11, covers[8]);
        categoryList.add(a);

        a = new Category("Услуги специалистов", 17, covers[9]);
        categoryList.add(a);

        adapter.notifyDataSetChanged();
    }
}



    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;


         GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;

        }


        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }

        }
    }

