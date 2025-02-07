package com.example.player

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil


class SongsFragment : Fragment(),  SongsAdaptor.OnItemListener {

    private var songListFull: List<Song>? = null
    private var songList: List<Song>? = null
    private var bottomNav: BottomNavigationView? = null
    private lateinit var recycler_songs: RecyclerView
    private lateinit var adaptor: SongsAdaptor
    private lateinit var mService: AudioPlayerService
    private val TAG = "SongFragment"
    private var listener: SongsFragListener? = null
    interface SongsFragListener: IMainActivity {
        fun onSongSelect(index: Int, text: String)
        fun onSettingsSort()
        fun getPlaylist(): MutableList<Song>?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = AudioPlayerService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        var view: View = inflater.inflate(R.layout.fragment_items, container, false)
        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
        var songsIcon: MenuItem =  nav.menu.findItem(R.id.nav_favorites)
        songsIcon.isChecked = true

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        setHasOptionsMenu(true)
        toolbar.title = resources.getString(R.string.toolbar_name_browse)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        recycler_songs = view.findViewById(R.id.recycler_songs)
        updateRecViewer()

        return view
    }

    fun updateRecViewer() {
        if (listener?.isService() == true) {
            songList = listener?.getPlaylist()
            songListFull = songList?.map { it.copy() }
            adaptor = SongsAdaptor((songList as MutableList<Song>?)!!, this)
            recycler_songs.adapter = adaptor
            recycler_songs.layoutManager = LinearLayoutManager(context)
            recycler_songs.setHasFixedSize(true)
        }
    }

    override fun onItemClick(position: Int, uri: String) {
        songListFull?.forEachIndexed { idx, element ->
            if (element.uri.toString() == uri) {
                listener?.onSongSelect(idx, uri)
                return@forEachIndexed
            }
        }
        UIUtil.hideKeyboard(requireActivity())

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // other class must implement this
        listener = context as SongsFragListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_songs, menu)
        val searchItem = menu.findItem(R.id.search_bar_songs)
        val searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adaptor.filter.filter(newText)
                return false
            }
        })
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val sortBy = sharedpreferences.getString(resources.getString(R.string.save_state_sort_key),"")
        //TODO - the below seems stupid.
        if ( sortBy == getString(R.string.sort_artist_asc) ){
            menu.findItem(R.id.sortArtistAscId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_artist_desc) ){
            menu.findItem(R.id.sortArtistDescId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_title_asc) ){
            menu.findItem(R.id.sortTitleAscId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_title_desc) ){
            menu.findItem(R.id.sortTitleDescId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_recent_most) ){
            menu.findItem(R.id.sortDateRecentId).isChecked = true
        }
        else {
            menu.findItem(R.id.sortDateOldestId).isChecked = true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun handleSortClick(sortBy: String){
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = sharedpreferences.edit()
        editor.putString(resources.getString(R.string.save_state_sort_key), sortBy)
        editor.commit()
        listener?.onSettingsSort()

        updateRecViewer()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.sortArtistAscId -> {
                item.isChecked = true
                handleSortClick(resources.getString(R.string.sort_artist_asc))
                return false
            }
            R.id.sortArtistDescId -> {
                item.isChecked = true
                handleSortClick(resources.getString(R.string.sort_artist_desc))
                return false
            }
            R.id.sortTitleAscId -> {
                item.isChecked = true
                handleSortClick(resources.getString(R.string.sort_title_asc))
                return false
            }
            R.id.sortTitleDescId -> {
                item.isChecked = true
                handleSortClick(resources.getString(R.string.sort_title_desc))
                return false
            }
            R.id.sortDateRecentId -> {
                item.isChecked = true
                handleSortClick(resources.getString(R.string.sort_recent_most))
                return false
            }
            R.id.sortDateOldestId -> {
                item.isChecked = true
                handleSortClick(resources.getString(R.string.sort_recent_least))
                return false
            }
            R.id.settingsId -> {
                listener?.handleSettingsClick()
                return false
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}


