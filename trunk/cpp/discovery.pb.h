// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: discovery.proto

#ifndef PROTOBUF_discovery_2eproto__INCLUDED
#define PROTOBUF_discovery_2eproto__INCLUDED

#include <string>

#include <google/protobuf/stubs/common.h>

#if GOOGLE_PROTOBUF_VERSION < 2004000
#error This file was generated by a newer version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please update
#error your headers.
#endif
#if 2004001 < GOOGLE_PROTOBUF_MIN_PROTOC_VERSION
#error This file was generated by an older version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please
#error regenerate this file with a newer version of protoc.
#endif

#include <google/protobuf/generated_message_util.h>
#include <google/protobuf/repeated_field.h>
#include <google/protobuf/extension_set.h>
#include <google/protobuf/generated_message_reflection.h>
// @@protoc_insertion_point(includes)

namespace guiprotocol {

// Internal implementation detail -- do not call these.
void  protobuf_AddDesc_discovery_2eproto();
void protobuf_AssignDesc_discovery_2eproto();
void protobuf_ShutdownFile_discovery_2eproto();

class DiscoveryRequest;
class DiscoveryResponse;

enum DiscoveryRequest_DiscoveryAppType {
  DiscoveryRequest_DiscoveryAppType_GUI = 0
};
bool DiscoveryRequest_DiscoveryAppType_IsValid(int value);
const DiscoveryRequest_DiscoveryAppType DiscoveryRequest_DiscoveryAppType_DiscoveryAppType_MIN = DiscoveryRequest_DiscoveryAppType_GUI;
const DiscoveryRequest_DiscoveryAppType DiscoveryRequest_DiscoveryAppType_DiscoveryAppType_MAX = DiscoveryRequest_DiscoveryAppType_GUI;
const int DiscoveryRequest_DiscoveryAppType_DiscoveryAppType_ARRAYSIZE = DiscoveryRequest_DiscoveryAppType_DiscoveryAppType_MAX + 1;

const ::google::protobuf::EnumDescriptor* DiscoveryRequest_DiscoveryAppType_descriptor();
inline const ::std::string& DiscoveryRequest_DiscoveryAppType_Name(DiscoveryRequest_DiscoveryAppType value) {
  return ::google::protobuf::internal::NameOfEnum(
    DiscoveryRequest_DiscoveryAppType_descriptor(), value);
}
inline bool DiscoveryRequest_DiscoveryAppType_Parse(
    const ::std::string& name, DiscoveryRequest_DiscoveryAppType* value) {
  return ::google::protobuf::internal::ParseNamedEnum<DiscoveryRequest_DiscoveryAppType>(
    DiscoveryRequest_DiscoveryAppType_descriptor(), name, value);
}
// ===================================================================

class DiscoveryRequest : public ::google::protobuf::Message {
 public:
  DiscoveryRequest();
  virtual ~DiscoveryRequest();
  
  DiscoveryRequest(const DiscoveryRequest& from);
  
  inline DiscoveryRequest& operator=(const DiscoveryRequest& from) {
    CopyFrom(from);
    return *this;
  }
  
  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }
  
  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }
  
  static const ::google::protobuf::Descriptor* descriptor();
  static const DiscoveryRequest& default_instance();
  
  void Swap(DiscoveryRequest* other);
  
  // implements Message ----------------------------------------------
  
  DiscoveryRequest* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const DiscoveryRequest& from);
  void MergeFrom(const DiscoveryRequest& from);
  void Clear();
  bool IsInitialized() const;
  
  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:
  
  ::google::protobuf::Metadata GetMetadata() const;
  
  // nested types ----------------------------------------------------
  
  typedef DiscoveryRequest_DiscoveryAppType DiscoveryAppType;
  static const DiscoveryAppType GUI = DiscoveryRequest_DiscoveryAppType_GUI;
  static inline bool DiscoveryAppType_IsValid(int value) {
    return DiscoveryRequest_DiscoveryAppType_IsValid(value);
  }
  static const DiscoveryAppType DiscoveryAppType_MIN =
    DiscoveryRequest_DiscoveryAppType_DiscoveryAppType_MIN;
  static const DiscoveryAppType DiscoveryAppType_MAX =
    DiscoveryRequest_DiscoveryAppType_DiscoveryAppType_MAX;
  static const int DiscoveryAppType_ARRAYSIZE =
    DiscoveryRequest_DiscoveryAppType_DiscoveryAppType_ARRAYSIZE;
  static inline const ::google::protobuf::EnumDescriptor*
  DiscoveryAppType_descriptor() {
    return DiscoveryRequest_DiscoveryAppType_descriptor();
  }
  static inline const ::std::string& DiscoveryAppType_Name(DiscoveryAppType value) {
    return DiscoveryRequest_DiscoveryAppType_Name(value);
  }
  static inline bool DiscoveryAppType_Parse(const ::std::string& name,
      DiscoveryAppType* value) {
    return DiscoveryRequest_DiscoveryAppType_Parse(name, value);
  }
  
  // accessors -------------------------------------------------------
  
  // optional string appName = 2;
  inline bool has_appname() const;
  inline void clear_appname();
  static const int kAppNameFieldNumber = 2;
  inline const ::std::string& appname() const;
  inline void set_appname(const ::std::string& value);
  inline void set_appname(const char* value);
  inline void set_appname(const char* value, size_t size);
  inline ::std::string* mutable_appname();
  inline ::std::string* release_appname();
  
  // optional .guiprotocol.DiscoveryRequest.DiscoveryAppType appType = 3;
  inline bool has_apptype() const;
  inline void clear_apptype();
  static const int kAppTypeFieldNumber = 3;
  inline ::guiprotocol::DiscoveryRequest_DiscoveryAppType apptype() const;
  inline void set_apptype(::guiprotocol::DiscoveryRequest_DiscoveryAppType value);
  
  // @@protoc_insertion_point(class_scope:guiprotocol.DiscoveryRequest)
 private:
  inline void set_has_appname();
  inline void clear_has_appname();
  inline void set_has_apptype();
  inline void clear_has_apptype();
  
  ::google::protobuf::UnknownFieldSet _unknown_fields_;
  
  ::std::string* appname_;
  int apptype_;
  
  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(2 + 31) / 32];
  
  friend void  protobuf_AddDesc_discovery_2eproto();
  friend void protobuf_AssignDesc_discovery_2eproto();
  friend void protobuf_ShutdownFile_discovery_2eproto();
  
  void InitAsDefaultInstance();
  static DiscoveryRequest* default_instance_;
};
// -------------------------------------------------------------------

class DiscoveryResponse : public ::google::protobuf::Message {
 public:
  DiscoveryResponse();
  virtual ~DiscoveryResponse();
  
  DiscoveryResponse(const DiscoveryResponse& from);
  
  inline DiscoveryResponse& operator=(const DiscoveryResponse& from) {
    CopyFrom(from);
    return *this;
  }
  
  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }
  
  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }
  
  static const ::google::protobuf::Descriptor* descriptor();
  static const DiscoveryResponse& default_instance();
  
  void Swap(DiscoveryResponse* other);
  
  // implements Message ----------------------------------------------
  
  DiscoveryResponse* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const DiscoveryResponse& from);
  void MergeFrom(const DiscoveryResponse& from);
  void Clear();
  bool IsInitialized() const;
  
  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:
  
  ::google::protobuf::Metadata GetMetadata() const;
  
  // nested types ----------------------------------------------------
  
  // accessors -------------------------------------------------------
  
  // required string serverIPAddress = 1;
  inline bool has_serveripaddress() const;
  inline void clear_serveripaddress();
  static const int kServerIPAddressFieldNumber = 1;
  inline const ::std::string& serveripaddress() const;
  inline void set_serveripaddress(const ::std::string& value);
  inline void set_serveripaddress(const char* value);
  inline void set_serveripaddress(const char* value, size_t size);
  inline ::std::string* mutable_serveripaddress();
  inline ::std::string* release_serveripaddress();
  
  // required int32 serverPortNumber = 2;
  inline bool has_serverportnumber() const;
  inline void clear_serverportnumber();
  static const int kServerPortNumberFieldNumber = 2;
  inline ::google::protobuf::int32 serverportnumber() const;
  inline void set_serverportnumber(::google::protobuf::int32 value);
  
  // @@protoc_insertion_point(class_scope:guiprotocol.DiscoveryResponse)
 private:
  inline void set_has_serveripaddress();
  inline void clear_has_serveripaddress();
  inline void set_has_serverportnumber();
  inline void clear_has_serverportnumber();
  
  ::google::protobuf::UnknownFieldSet _unknown_fields_;
  
  ::std::string* serveripaddress_;
  ::google::protobuf::int32 serverportnumber_;
  
  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(2 + 31) / 32];
  
  friend void  protobuf_AddDesc_discovery_2eproto();
  friend void protobuf_AssignDesc_discovery_2eproto();
  friend void protobuf_ShutdownFile_discovery_2eproto();
  
  void InitAsDefaultInstance();
  static DiscoveryResponse* default_instance_;
};
// ===================================================================


// ===================================================================

// DiscoveryRequest

// optional string appName = 2;
inline bool DiscoveryRequest::has_appname() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void DiscoveryRequest::set_has_appname() {
  _has_bits_[0] |= 0x00000001u;
}
inline void DiscoveryRequest::clear_has_appname() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void DiscoveryRequest::clear_appname() {
  if (appname_ != &::google::protobuf::internal::kEmptyString) {
    appname_->clear();
  }
  clear_has_appname();
}
inline const ::std::string& DiscoveryRequest::appname() const {
  return *appname_;
}
inline void DiscoveryRequest::set_appname(const ::std::string& value) {
  set_has_appname();
  if (appname_ == &::google::protobuf::internal::kEmptyString) {
    appname_ = new ::std::string;
  }
  appname_->assign(value);
}
inline void DiscoveryRequest::set_appname(const char* value) {
  set_has_appname();
  if (appname_ == &::google::protobuf::internal::kEmptyString) {
    appname_ = new ::std::string;
  }
  appname_->assign(value);
}
inline void DiscoveryRequest::set_appname(const char* value, size_t size) {
  set_has_appname();
  if (appname_ == &::google::protobuf::internal::kEmptyString) {
    appname_ = new ::std::string;
  }
  appname_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* DiscoveryRequest::mutable_appname() {
  set_has_appname();
  if (appname_ == &::google::protobuf::internal::kEmptyString) {
    appname_ = new ::std::string;
  }
  return appname_;
}
inline ::std::string* DiscoveryRequest::release_appname() {
  clear_has_appname();
  if (appname_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = appname_;
    appname_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}

// optional .guiprotocol.DiscoveryRequest.DiscoveryAppType appType = 3;
inline bool DiscoveryRequest::has_apptype() const {
  return (_has_bits_[0] & 0x00000002u) != 0;
}
inline void DiscoveryRequest::set_has_apptype() {
  _has_bits_[0] |= 0x00000002u;
}
inline void DiscoveryRequest::clear_has_apptype() {
  _has_bits_[0] &= ~0x00000002u;
}
inline void DiscoveryRequest::clear_apptype() {
  apptype_ = 0;
  clear_has_apptype();
}
inline ::guiprotocol::DiscoveryRequest_DiscoveryAppType DiscoveryRequest::apptype() const {
  return static_cast< ::guiprotocol::DiscoveryRequest_DiscoveryAppType >(apptype_);
}
inline void DiscoveryRequest::set_apptype(::guiprotocol::DiscoveryRequest_DiscoveryAppType value) {
  GOOGLE_DCHECK(::guiprotocol::DiscoveryRequest_DiscoveryAppType_IsValid(value));
  set_has_apptype();
  apptype_ = value;
}

// -------------------------------------------------------------------

// DiscoveryResponse

// required string serverIPAddress = 1;
inline bool DiscoveryResponse::has_serveripaddress() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void DiscoveryResponse::set_has_serveripaddress() {
  _has_bits_[0] |= 0x00000001u;
}
inline void DiscoveryResponse::clear_has_serveripaddress() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void DiscoveryResponse::clear_serveripaddress() {
  if (serveripaddress_ != &::google::protobuf::internal::kEmptyString) {
    serveripaddress_->clear();
  }
  clear_has_serveripaddress();
}
inline const ::std::string& DiscoveryResponse::serveripaddress() const {
  return *serveripaddress_;
}
inline void DiscoveryResponse::set_serveripaddress(const ::std::string& value) {
  set_has_serveripaddress();
  if (serveripaddress_ == &::google::protobuf::internal::kEmptyString) {
    serveripaddress_ = new ::std::string;
  }
  serveripaddress_->assign(value);
}
inline void DiscoveryResponse::set_serveripaddress(const char* value) {
  set_has_serveripaddress();
  if (serveripaddress_ == &::google::protobuf::internal::kEmptyString) {
    serveripaddress_ = new ::std::string;
  }
  serveripaddress_->assign(value);
}
inline void DiscoveryResponse::set_serveripaddress(const char* value, size_t size) {
  set_has_serveripaddress();
  if (serveripaddress_ == &::google::protobuf::internal::kEmptyString) {
    serveripaddress_ = new ::std::string;
  }
  serveripaddress_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* DiscoveryResponse::mutable_serveripaddress() {
  set_has_serveripaddress();
  if (serveripaddress_ == &::google::protobuf::internal::kEmptyString) {
    serveripaddress_ = new ::std::string;
  }
  return serveripaddress_;
}
inline ::std::string* DiscoveryResponse::release_serveripaddress() {
  clear_has_serveripaddress();
  if (serveripaddress_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = serveripaddress_;
    serveripaddress_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}

// required int32 serverPortNumber = 2;
inline bool DiscoveryResponse::has_serverportnumber() const {
  return (_has_bits_[0] & 0x00000002u) != 0;
}
inline void DiscoveryResponse::set_has_serverportnumber() {
  _has_bits_[0] |= 0x00000002u;
}
inline void DiscoveryResponse::clear_has_serverportnumber() {
  _has_bits_[0] &= ~0x00000002u;
}
inline void DiscoveryResponse::clear_serverportnumber() {
  serverportnumber_ = 0;
  clear_has_serverportnumber();
}
inline ::google::protobuf::int32 DiscoveryResponse::serverportnumber() const {
  return serverportnumber_;
}
inline void DiscoveryResponse::set_serverportnumber(::google::protobuf::int32 value) {
  set_has_serverportnumber();
  serverportnumber_ = value;
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace guiprotocol

#ifndef SWIG
namespace google {
namespace protobuf {

template <>
inline const EnumDescriptor* GetEnumDescriptor< ::guiprotocol::DiscoveryRequest_DiscoveryAppType>() {
  return ::guiprotocol::DiscoveryRequest_DiscoveryAppType_descriptor();
}

}  // namespace google
}  // namespace protobuf
#endif  // SWIG

// @@protoc_insertion_point(global_scope)

#endif  // PROTOBUF_discovery_2eproto__INCLUDED