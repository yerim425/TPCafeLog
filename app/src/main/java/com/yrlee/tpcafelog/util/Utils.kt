package com.yrlee.tpcafelog.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

object Utils {

    private lateinit var appContext: Context


    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun getRealPathFromUri(uri: Uri): String{

        // android 10 version 부터 uri 에 해당하는 실제경로를 얻어오는 것이 불가능함
        // 그래서 별도의 임시파일을 만들어 uri에 해당하는 이미지 파일의 데이터를 복사해서 이 임시파일을 업로드함

        // 원본파일(선택한 파일)의 [파일명.확장자] 알아내기 [MediaStore DB에서 "display_name" 컬룸의 값을 얻어오는 작엄]
        // CursorLoader: uri에 해당하는 DB의 SELECT 쿼리기능을 통해 결과표(Cursor)를 가져오는 클래스
        val cursorLoader: CursorLoader = CursorLoader(appContext, uri, null, null, null, null)
        val cursor = cursorLoader.loadInBackground()
        val fileName = cursor?.run{
            moveToFirst()
            getString(getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)) // 못찾아오면 down되지 않고 throw
        }
        // 저장할 임시파일
        val file: File = File(appContext.externalCacheDir, fileName!!)//File(경로, 파일명)
        // 경로 확인
        //AlertDialog.Builder(this).setMessage(file.toString()).create().show()

        // 임시파일에 선택한 이미지파일의 byte 값들을 copy
        val inputStream = appContext.contentResolver.openInputStream(uri) ?: return ""// 원본파일에 연결된 입력 스트림
        val outputStream = FileOutputStream(file) // 임시파일에 연결된 출력 스트림

        while(true){
            val buf: ByteArray = ByteArray(1024) // 1024 byte 배열 (1KB)
            val len = inputStream.read(buf) // buf 사이즈만큼 읽어서 buf 배열 안에 넣어줌 .. 만약 1024개가 안되면 그만큼만 읽어오고 그 개수를 리턴해줌
            if(len==-1) break
            outputStream.write(buf, 0, len)
        }

        inputStream.close()
        outputStream.close()

        return file.absolutePath
    }

    fun getNowDateTimeString(): String{
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    }

    fun jsonToRequestBody(json: String): RequestBody {
        return json.toRequestBody("text/plain".toMediaTypeOrNull())
    }
    fun filePathToMultipartBody(path: String): MultipartBody.Part{
        val file = File(path)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("img", file.name, requestBody)
    }
}