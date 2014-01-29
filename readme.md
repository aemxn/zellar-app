# Zellar (unofficial) v1 Alpha

## Revision Changes
1. [Action Bar Style Generator](http://jgilfelt.github.io/android-actionbarstylegenerator/) (30/9/2013)
2. Camera capture and upload to server
3. **Rev 1.4**
	+ Implemented Picasso GridView into Dasboard (2/10/2013)
4. **Rev 1.4.1**
	+ Small change. Added HoloEverywhere Library. Changed `@style` in `Layout.xml`
5. **Rev 1.5**
	+ Improvised Picasso Gridview. `OnClickListener` goes to `UserProfile` together with Item ID (4/10/2013)
6. **Rev 1.9** (7/10/2013)
	+ Added `ConnectionDetector` in LoginActivity and RegisterActivity if internet fails
	+ Implemented `ListView` to view categories and its content
7. **Rev 2.0** (9/10/2013)
	+ I don't know what I do. But I took time to understand the flow of ListView and Login Register Activity
	+ Changed UploadImageActivity layout a bit
8. **Rev 2.1** (10/10/2013)
	+ Modified UploadImageActivity to use Relative Layout instead. Easier
9. **Rev 2.5** (13/10/2013)
	+ Setup server and database hosting. Check Database/Server Connections section.
10. **Rev 2.7** (14/10/2013)
	+ Modified `upload.php` to move uploaded file to a directory
	+ Refactored classes name in client side to sync with server-side scripting
	+ Moved to BitBucket for private repo
11. **Rev 3.0** (22/10/2013)
	+ Modified `data.php` to retrieve data from mysql db and storing it in array by following original format
	+ Fixed `items` id to reset according to items
12. **Rev 3.4** (25/10/2013)
	+ Added Geolocation functionality
	+ Implemented `AsyncTask` and fail-safe into `LoginActivity` and `RegisterActivity`
13. **Rev 3.41** (28/10/2013)
	+ Implemented `AsyncTask` into `UploadImageActivity`
14.	**Rev 3.43** (1/11/2013)
	+ UploadImageActivity now accepts EditText. Passing text stuff to server.
	+ Fixed few algorithms to show item details
	+ Removed `DashboardActivity`. Changed `CategoryActivity` to be main.
15. **Rev 3.5** (12/11/2013)
	+ Enable thumbnail view in ItemListView activity
16. **Rev 3.8** (22/11/2013)
	+ Enable image cropping for upload from gallery and camera intent
	+ Implemented basic Socialize SDK
17. **Rev 3.9** (2/12/2013)
	+ Removed Socialized SDK
	+ Added view comment and submit comment
	+ Updated `CommentListActivity`
18. **Rev 3.91** (4/12/2013)
	+ Added GridView to `UserProfileActivity`
19. **Rev 4.0** (6/12/2013)
	+ Changed the package name to *Zellar* (Unofficial name)
	+ Moved all URL constants to `Constants` class
	+ Made a UI cleanup at Category List.
	+ Begin releasing to friends
20. **Rev 4.1** (9/12/2013)
	+ Added `ListViewAnimation` into all ListViews
	+ Added Cards UI for interface


## Issues
1. ☑ Rev 1.9 (7/10/2013) - ~~OnClickListener to UserProfile Activity~~
	+ Solution: declaration in manifest.xml
2. ☑ Rev 2.0 (9/10/2013) - ~~App crashes when photo is chosen the second time~~
	+ Solution: Cursor pointing in UploadImage.getPath() function
3. Values are stored in phone's memory. Does not do any good for Geocoder.
4. ☑ - ~~`NetworkOnMainThreadException` in CategoryActivity~~
	+ Cannot run network on main thread (onCreate). Added a new AsyncTask function that handles NetCheck
5. ☑ Rev 2.0 (9/10/2013) - ~~Uploading a photo returns fucking `NULL` (panic)~~
6. ☑ ~~`RegisterActivity` is showing email is invalid. Fuark.~~
	+ `!filter_var($email, FILTER_VALIDATE_EMAIL)` for the win
7. After register, it logs in with the previous ID
8. ☑ Crashes: Taking photo and Uploading part
9. 000webhost server is always down. Seems like we're having a bandwidth issue.


## TODO
1. Customize UI/UX
	+ Splash screen
	+ Loading screen
	+ Navigation drawer
	+ CardsUI
	+ Tabbed bar
	+ Search function
	+ Up button
	+ Refresh button
	+ Remember Me/Pw reset
		http://www.learn2crack.com/2013/08/develop-android-login-registration-with-php-mysql.html
- ☑ ~~Remote server setup~~ [rev-2.5]
- CRUD stuff
	+ ☑ ~~Capture, save in different filenames~~ [rev-2.7]
	+ ☑ ~~Save photo in directories~~ [rev-2.7]
	+ ☑ ~~Put String it in MySQL. Retreive using PHP/JSON~~ [rev-3.43]
	+ ☑ ~~Update data.php in JSON format~~ [rev-3.0]
- ☑ ~~Customize ListView to list categories~~
	+ ☑ Retrieve from Database to PHP (server side)
- Warnings/Notifications
	+ ☑ ~~Errors if connection not found~~ [rev-1.9]
	+ ☑ ~~ProgressDialog~~
	+ Connection Timeout
- SharedPreference?
- ☑ ~~AsyncTask the Login/Register~~ [rev-3.4]
- Modify UploadImageActivity.java
	+ ☑ ~~Fields to be added~~ [rev-2.1] [rev-3.4 **backend**]
		+ ☑ Item name
		+ ☑ Description
		+ ☑ Categories
		+ Social Share
		+ ☑ Price
	+ ☑ save it in `upload.php` via Mysql
- Create a use case (never too late brah)
- Upgrade server design
	+ ORM
	+ PDO
	+ Framework
- Clean codes, make use of OOP redesign
- ☑ Comment section
- Notification system
- ☑ Crop photo item
- Bidding system
- Private messaging

## Database/Server Connections
### Testing purpose

#### Database Information
1. Server Type: MySQL
2. Connection Name: db4freehost
3. Host Name/IP Address: db4free.net
4. Port: 3306
5. User Name: webshopper
6. Password: polo90
7. Server Version: 5.6.14
8. Protocol: 10
9. Info: db4free.net via TCP/IP

#### Server Hosting Information
1. FTP host name: ftp://uw0tm8.freeiz.com/
2. FTP host IP: 31.170.160.108
3. FTP user name: a8330584
4. FTP password: polo90
5. Folder to upload files: public_html

**New Server** *(cleaned)*

1. FTP host name: 	ftp://zellar.comze.com/
2. FTP user name: 	a2381866
3. FTP password: 	zellar123
4. Folder to upload files: 	public_html

#### Files Related to Server side
Moved to `Constants` class

#### Backup procedure
**Client**

1. Update project `readme.md`
2. Copy project folder to local disk
3. Rename copied folder according to revision change
4. Commit source project folder to BitBucket

**Server**

1. Copy server folder to Github folder
2. Upload via FTP
3. Commit to Bitbucket

***

### Tutorials
- http://www.androidhive.info/
- http://sunil-android.blogspot.com/2013/03/image-upload-on-server.html
- http://www.mybringback.com/series/android-basics/
- http://anujarosha.wordpress.com/2012/01/27/handling-http-post-method-in-android/
- http://wptrafficanalyzer.in/blog/android-lazy-loading-images-and-text-in-listview-from-http-json-data/

### Documentation
- http://developer.android.com/‎

### Forum
- http://www.stackoverflow.com/

### Basic Android as per lesson
- CRUD + PHP/MySQL + JSON
- Moving from views to views
- AsyncTask
- Lrn2usecase

---
Aiman Baharum (c) 2013