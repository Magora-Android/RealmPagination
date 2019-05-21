# RealmPagination
Just a copy of Google Paging library (https://developer.android.com/topic/libraries/architecture/paging) entirely rewritten in Kotlin and intended to be used with Realm.


# Adding to project
To use RealmPagination you need to add as a dependency:

`implementation 'com.github.magora-android:realmpagination:1.0.0'`

And it's available on Jitpack, so you need to add

    allprojects {
        repositories {
            // ...
            maven { url "https://jitpack.io" }
        }
        // ...
    }


# How To
First you need to create a DataSourceFactory

    class UsersListDataSourceFactory : RealmDataSource.Factory<Int, User>() {

        override fun create(): RealmDataSource<Int, User> {
            val result = object : RealmPageKeyedDataSource<Int, User>() {
                override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, User>) {
                    //...
                }

                override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
                    callback.onResult(0, null)
                }

    			override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
                    //...
                }
            }
            return result
        }

        override fun destroy() {
            //...
        }
    }

    val dsFactory = UsersListDataSourceFactory()

Then you need to create a Paged List

    private const val INITIAL_PAGE_SIZE = 50
    private const val PAGE_SIZE = 30
    private const val PREFETCH_DISTANCE = 10

    val config = RealmPagedList.Config.Builder()
                    .setInitialLoadSizeHint(INITIAL_PAGE_SIZE)
                    .setPageSize(PAGE_SIZE)
                    .setPrefetchDistance(PREFETCH_DISTANCE)
                    .build()

    val pagedList = RealmPagedListBuilder(dsFactory, config)
                    .setInitialLoadKey(0)
                    .setRealmData(localStorage.getUsers())
                    .build()

Then create an adapter

    class AdapterUserList(data: RealmPagedList<*, User>) : BaseRealmListenableAdapter<User, RecyclerView.ViewHolder>(data) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //...
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //...
        }
    }

	val adapter = AdapterUserList(pagedList)
	usersList.adapter = adapter

... and use the power of pagination