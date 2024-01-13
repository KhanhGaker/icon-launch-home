package one.zagura.IonLauncher.provider.items

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import one.zagura.IonLauncher.data.items.ContactItem

object ContactsLoader {

    fun load(context: Context, requiresStar: Boolean = false): List<ContactItem> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            context.checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED)
            return emptyList()
        val cur = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.STARRED,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                ContactsContract.Contacts.PHOTO_ID), null, null, null)
            ?: return emptyList()

        val contactMap = HashMap<String, ContactItem>()

        if (cur.count != 0) {
            while (cur.moveToNext()) {
                val starred = cur.getInt(3) != 0
                if (requiresStar && !starred) {
                    continue
                }
                val lookupKey = cur.getString(0)
                val name = cur.getString(1)
                if (name.isNullOrBlank()) continue
                val phone = cur.getString(2)
                val photoId = cur.getString(5)
                val iconUri: Uri? = if (photoId != null) {
                    ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId.toLong())
                } else null

                val contact = ContactItem(name, lookupKey, phone, starred, iconUri)

                if (!contactMap.containsKey(lookupKey)) {
                    contactMap[lookupKey] = contact
                }
            }
        }
        cur.close()

        val nicknameCur = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Nickname.NAME, ContactsContract.Data.LOOKUP_KEY),
            "${ContactsContract.Data.MIMETYPE}=?",
            arrayOf(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE), null)

        if (nicknameCur != null) {
            if (nicknameCur.count != 0) {
                while (nicknameCur.moveToNext()) {
                    val lookupKey = nicknameCur.getString(1)
                    val nickname = nicknameCur.getString(0)
                    if (nickname != null && lookupKey != null && contactMap.containsKey(lookupKey)) {
                        contactMap[lookupKey]!!.label = nickname
                    }
                }
            }
            nicknameCur.close()
        }

        return contactMap.values.toList()
    }
}