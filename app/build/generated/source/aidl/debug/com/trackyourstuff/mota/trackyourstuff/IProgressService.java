/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/mota/AndroidStudioProjects/TrackYourStuff/app/src/main/aidl/com/trackyourstuff/mota/trackyourstuff/IProgressService.aidl
 */
package com.trackyourstuff.mota.trackyourstuff;
// Declare any non-default types here with import statements

public interface IProgressService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.trackyourstuff.mota.trackyourstuff.IProgressService
{
private static final java.lang.String DESCRIPTOR = "com.trackyourstuff.mota.trackyourstuff.IProgressService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.trackyourstuff.mota.trackyourstuff.IProgressService interface,
 * generating a proxy if needed.
 */
public static com.trackyourstuff.mota.trackyourstuff.IProgressService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.trackyourstuff.mota.trackyourstuff.IProgressService))) {
return ((com.trackyourstuff.mota.trackyourstuff.IProgressService)iin);
}
return new com.trackyourstuff.mota.trackyourstuff.IProgressService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getProgress:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getProgress();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_startProgress:
{
data.enforceInterface(DESCRIPTOR);
this.startProgress();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.trackyourstuff.mota.trackyourstuff.IProgressService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public long getProgress() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getProgress, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void startProgress() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startProgress, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_startProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public long getProgress() throws android.os.RemoteException;
public void startProgress() throws android.os.RemoteException;
}
