# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: IM.File.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


import IM.BaseDefine_pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='IM.File.proto',
  package='IM.File',
  serialized_pb=_b('\n\rIM.File.proto\x12\x07IM.File\x1a\x13IM.BaseDefine.proto\"d\n\x0eIMFileLoginReq\x12\x0f\n\x07user_id\x18\x01 \x02(\r\x12\x0f\n\x07task_id\x18\x02 \x02(\t\x12\x30\n\tfile_role\x18\x03 \x02(\x0e\x32\x1d.IM.BaseDefine.ClientFileRole\"6\n\x0eIMFileLoginRsp\x12\x13\n\x0bresult_code\x18\x01 \x02(\r\x12\x0f\n\x07task_id\x18\x02 \x02(\t\"^\n\x0bIMFileState\x12-\n\x05state\x18\x01 \x02(\x0e\x32\x1e.IM.BaseDefine.ClientFileState\x12\x0f\n\x07task_id\x18\x02 \x02(\t\x12\x0f\n\x07user_id\x18\x03 \x02(\r\"\x8d\x01\n\x11IMFilePullDataReq\x12\x0f\n\x07task_id\x18\x01 \x02(\t\x12\x0f\n\x07user_id\x18\x02 \x02(\r\x12\x33\n\ntrans_mode\x18\x03 \x02(\x0e\x32\x1f.IM.BaseDefine.TransferFileType\x12\x0e\n\x06offset\x18\x04 \x02(\r\x12\x11\n\tdata_size\x18\x05 \x02(\r\"m\n\x11IMFilePullDataRsp\x12\x13\n\x0bresult_code\x18\x01 \x02(\r\x12\x0f\n\x07task_id\x18\x02 \x02(\t\x12\x0f\n\x07user_id\x18\x03 \x02(\r\x12\x0e\n\x06offset\x18\x04 \x02(\r\x12\x11\n\tfile_data\x18\x05 \x02(\x0c\"\x90\x01\n\tIMFileReq\x12\x14\n\x0c\x66rom_user_id\x18\x01 \x02(\r\x12\x12\n\nto_user_id\x18\x02 \x02(\r\x12\x11\n\tfile_name\x18\x03 \x02(\t\x12\x11\n\tfile_size\x18\x04 \x02(\r\x12\x33\n\ntrans_mode\x18\x05 \x02(\x0e\x32\x1f.IM.BaseDefine.TransferFileType\"\xd0\x01\n\tIMFileRsp\x12\x13\n\x0bresult_code\x18\x01 \x02(\r\x12\x14\n\x0c\x66rom_user_id\x18\x02 \x02(\r\x12\x12\n\nto_user_id\x18\x03 \x02(\r\x12\x11\n\tfile_name\x18\x04 \x02(\t\x12\x0f\n\x07task_id\x18\x05 \x02(\t\x12+\n\x0cip_addr_list\x18\x06 \x03(\x0b\x32\x15.IM.BaseDefine.IpAddr\x12\x33\n\ntrans_mode\x18\x07 \x02(\x0e\x32\x1f.IM.BaseDefine.TransferFileType\"\xe8\x01\n\x0cIMFileNotify\x12\x14\n\x0c\x66rom_user_id\x18\x01 \x02(\r\x12\x12\n\nto_user_id\x18\x02 \x02(\r\x12\x11\n\tfile_name\x18\x03 \x02(\t\x12\x11\n\tfile_size\x18\x04 \x02(\r\x12\x0f\n\x07task_id\x18\x05 \x02(\t\x12+\n\x0cip_addr_list\x18\x06 \x03(\x0b\x32\x15.IM.BaseDefine.IpAddr\x12\x33\n\ntrans_mode\x18\x07 \x02(\x0e\x32\x1f.IM.BaseDefine.TransferFileType\x12\x15\n\roffline_ready\x18\x08 \x02(\r\";\n\x13IMFileHasOfflineReq\x12\x0f\n\x07user_id\x18\x01 \x02(\r\x12\x13\n\x0b\x61ttach_data\x18\x14 \x01(\x0c\"\xa3\x01\n\x13IMFileHasOfflineRsp\x12\x0f\n\x07user_id\x18\x01 \x02(\r\x12\x39\n\x11offline_file_list\x18\x02 \x03(\x0b\x32\x1e.IM.BaseDefine.OfflineFileInfo\x12+\n\x0cip_addr_list\x18\x03 \x03(\x0b\x32\x15.IM.BaseDefine.IpAddr\x12\x13\n\x0b\x61ttach_data\x18\x14 \x01(\x0c\"v\n\x13IMFileAddOfflineReq\x12\x14\n\x0c\x66rom_user_id\x18\x01 \x02(\r\x12\x12\n\nto_user_id\x18\x02 \x02(\r\x12\x0f\n\x07task_id\x18\x03 \x02(\t\x12\x11\n\tfile_name\x18\x04 \x02(\t\x12\x11\n\tfile_size\x18\x05 \x02(\r\"P\n\x13IMFileDelOfflineReq\x12\x14\n\x0c\x66rom_user_id\x18\x01 \x02(\r\x12\x12\n\nto_user_id\x18\x02 \x02(\r\x12\x0f\n\x07task_id\x18\x03 \x02(\tB\x1b\n\x17\x63om.mogujie.tt.protobufH\x03')
  ,
  dependencies=[IM.BaseDefine_pb2.DESCRIPTOR,])
_sym_db.RegisterFileDescriptor(DESCRIPTOR)




_IMFILELOGINREQ = _descriptor.Descriptor(
  name='IMFileLoginReq',
  full_name='IM.File.IMFileLoginReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='user_id', full_name='IM.File.IMFileLoginReq.user_id', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFileLoginReq.task_id', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_role', full_name='IM.File.IMFileLoginReq.file_role', index=2,
      number=3, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=47,
  serialized_end=147,
)


_IMFILELOGINRSP = _descriptor.Descriptor(
  name='IMFileLoginRsp',
  full_name='IM.File.IMFileLoginRsp',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='result_code', full_name='IM.File.IMFileLoginRsp.result_code', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFileLoginRsp.task_id', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=149,
  serialized_end=203,
)


_IMFILESTATE = _descriptor.Descriptor(
  name='IMFileState',
  full_name='IM.File.IMFileState',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='state', full_name='IM.File.IMFileState.state', index=0,
      number=1, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFileState.task_id', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='user_id', full_name='IM.File.IMFileState.user_id', index=2,
      number=3, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=205,
  serialized_end=299,
)


_IMFILEPULLDATAREQ = _descriptor.Descriptor(
  name='IMFilePullDataReq',
  full_name='IM.File.IMFilePullDataReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFilePullDataReq.task_id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='user_id', full_name='IM.File.IMFilePullDataReq.user_id', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='trans_mode', full_name='IM.File.IMFilePullDataReq.trans_mode', index=2,
      number=3, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='offset', full_name='IM.File.IMFilePullDataReq.offset', index=3,
      number=4, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='data_size', full_name='IM.File.IMFilePullDataReq.data_size', index=4,
      number=5, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=302,
  serialized_end=443,
)


_IMFILEPULLDATARSP = _descriptor.Descriptor(
  name='IMFilePullDataRsp',
  full_name='IM.File.IMFilePullDataRsp',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='result_code', full_name='IM.File.IMFilePullDataRsp.result_code', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFilePullDataRsp.task_id', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='user_id', full_name='IM.File.IMFilePullDataRsp.user_id', index=2,
      number=3, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='offset', full_name='IM.File.IMFilePullDataRsp.offset', index=3,
      number=4, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_data', full_name='IM.File.IMFilePullDataRsp.file_data', index=4,
      number=5, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=445,
  serialized_end=554,
)


_IMFILEREQ = _descriptor.Descriptor(
  name='IMFileReq',
  full_name='IM.File.IMFileReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='from_user_id', full_name='IM.File.IMFileReq.from_user_id', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='to_user_id', full_name='IM.File.IMFileReq.to_user_id', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_name', full_name='IM.File.IMFileReq.file_name', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_size', full_name='IM.File.IMFileReq.file_size', index=3,
      number=4, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='trans_mode', full_name='IM.File.IMFileReq.trans_mode', index=4,
      number=5, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=557,
  serialized_end=701,
)


_IMFILERSP = _descriptor.Descriptor(
  name='IMFileRsp',
  full_name='IM.File.IMFileRsp',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='result_code', full_name='IM.File.IMFileRsp.result_code', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='from_user_id', full_name='IM.File.IMFileRsp.from_user_id', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='to_user_id', full_name='IM.File.IMFileRsp.to_user_id', index=2,
      number=3, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_name', full_name='IM.File.IMFileRsp.file_name', index=3,
      number=4, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFileRsp.task_id', index=4,
      number=5, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ip_addr_list', full_name='IM.File.IMFileRsp.ip_addr_list', index=5,
      number=6, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='trans_mode', full_name='IM.File.IMFileRsp.trans_mode', index=6,
      number=7, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=704,
  serialized_end=912,
)


_IMFILENOTIFY = _descriptor.Descriptor(
  name='IMFileNotify',
  full_name='IM.File.IMFileNotify',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='from_user_id', full_name='IM.File.IMFileNotify.from_user_id', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='to_user_id', full_name='IM.File.IMFileNotify.to_user_id', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_name', full_name='IM.File.IMFileNotify.file_name', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_size', full_name='IM.File.IMFileNotify.file_size', index=3,
      number=4, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFileNotify.task_id', index=4,
      number=5, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ip_addr_list', full_name='IM.File.IMFileNotify.ip_addr_list', index=5,
      number=6, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='trans_mode', full_name='IM.File.IMFileNotify.trans_mode', index=6,
      number=7, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='offline_ready', full_name='IM.File.IMFileNotify.offline_ready', index=7,
      number=8, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=915,
  serialized_end=1147,
)


_IMFILEHASOFFLINEREQ = _descriptor.Descriptor(
  name='IMFileHasOfflineReq',
  full_name='IM.File.IMFileHasOfflineReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='user_id', full_name='IM.File.IMFileHasOfflineReq.user_id', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='attach_data', full_name='IM.File.IMFileHasOfflineReq.attach_data', index=1,
      number=20, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1149,
  serialized_end=1208,
)


_IMFILEHASOFFLINERSP = _descriptor.Descriptor(
  name='IMFileHasOfflineRsp',
  full_name='IM.File.IMFileHasOfflineRsp',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='user_id', full_name='IM.File.IMFileHasOfflineRsp.user_id', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='offline_file_list', full_name='IM.File.IMFileHasOfflineRsp.offline_file_list', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ip_addr_list', full_name='IM.File.IMFileHasOfflineRsp.ip_addr_list', index=2,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='attach_data', full_name='IM.File.IMFileHasOfflineRsp.attach_data', index=3,
      number=20, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1211,
  serialized_end=1374,
)


_IMFILEADDOFFLINEREQ = _descriptor.Descriptor(
  name='IMFileAddOfflineReq',
  full_name='IM.File.IMFileAddOfflineReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='from_user_id', full_name='IM.File.IMFileAddOfflineReq.from_user_id', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='to_user_id', full_name='IM.File.IMFileAddOfflineReq.to_user_id', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFileAddOfflineReq.task_id', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_name', full_name='IM.File.IMFileAddOfflineReq.file_name', index=3,
      number=4, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='file_size', full_name='IM.File.IMFileAddOfflineReq.file_size', index=4,
      number=5, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1376,
  serialized_end=1494,
)


_IMFILEDELOFFLINEREQ = _descriptor.Descriptor(
  name='IMFileDelOfflineReq',
  full_name='IM.File.IMFileDelOfflineReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='from_user_id', full_name='IM.File.IMFileDelOfflineReq.from_user_id', index=0,
      number=1, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='to_user_id', full_name='IM.File.IMFileDelOfflineReq.to_user_id', index=1,
      number=2, type=13, cpp_type=3, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='task_id', full_name='IM.File.IMFileDelOfflineReq.task_id', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1496,
  serialized_end=1576,
)

_IMFILELOGINREQ.fields_by_name['file_role'].enum_type = IM.BaseDefine_pb2._CLIENTFILEROLE
_IMFILESTATE.fields_by_name['state'].enum_type = IM.BaseDefine_pb2._CLIENTFILESTATE
_IMFILEPULLDATAREQ.fields_by_name['trans_mode'].enum_type = IM.BaseDefine_pb2._TRANSFERFILETYPE
_IMFILEREQ.fields_by_name['trans_mode'].enum_type = IM.BaseDefine_pb2._TRANSFERFILETYPE
_IMFILERSP.fields_by_name['ip_addr_list'].message_type = IM.BaseDefine_pb2._IPADDR
_IMFILERSP.fields_by_name['trans_mode'].enum_type = IM.BaseDefine_pb2._TRANSFERFILETYPE
_IMFILENOTIFY.fields_by_name['ip_addr_list'].message_type = IM.BaseDefine_pb2._IPADDR
_IMFILENOTIFY.fields_by_name['trans_mode'].enum_type = IM.BaseDefine_pb2._TRANSFERFILETYPE
_IMFILEHASOFFLINERSP.fields_by_name['offline_file_list'].message_type = IM.BaseDefine_pb2._OFFLINEFILEINFO
_IMFILEHASOFFLINERSP.fields_by_name['ip_addr_list'].message_type = IM.BaseDefine_pb2._IPADDR
DESCRIPTOR.message_types_by_name['IMFileLoginReq'] = _IMFILELOGINREQ
DESCRIPTOR.message_types_by_name['IMFileLoginRsp'] = _IMFILELOGINRSP
DESCRIPTOR.message_types_by_name['IMFileState'] = _IMFILESTATE
DESCRIPTOR.message_types_by_name['IMFilePullDataReq'] = _IMFILEPULLDATAREQ
DESCRIPTOR.message_types_by_name['IMFilePullDataRsp'] = _IMFILEPULLDATARSP
DESCRIPTOR.message_types_by_name['IMFileReq'] = _IMFILEREQ
DESCRIPTOR.message_types_by_name['IMFileRsp'] = _IMFILERSP
DESCRIPTOR.message_types_by_name['IMFileNotify'] = _IMFILENOTIFY
DESCRIPTOR.message_types_by_name['IMFileHasOfflineReq'] = _IMFILEHASOFFLINEREQ
DESCRIPTOR.message_types_by_name['IMFileHasOfflineRsp'] = _IMFILEHASOFFLINERSP
DESCRIPTOR.message_types_by_name['IMFileAddOfflineReq'] = _IMFILEADDOFFLINEREQ
DESCRIPTOR.message_types_by_name['IMFileDelOfflineReq'] = _IMFILEDELOFFLINEREQ

IMFileLoginReq = _reflection.GeneratedProtocolMessageType('IMFileLoginReq', (_message.Message,), dict(
  DESCRIPTOR = _IMFILELOGINREQ,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileLoginReq)
  ))
_sym_db.RegisterMessage(IMFileLoginReq)

IMFileLoginRsp = _reflection.GeneratedProtocolMessageType('IMFileLoginRsp', (_message.Message,), dict(
  DESCRIPTOR = _IMFILELOGINRSP,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileLoginRsp)
  ))
_sym_db.RegisterMessage(IMFileLoginRsp)

IMFileState = _reflection.GeneratedProtocolMessageType('IMFileState', (_message.Message,), dict(
  DESCRIPTOR = _IMFILESTATE,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileState)
  ))
_sym_db.RegisterMessage(IMFileState)

IMFilePullDataReq = _reflection.GeneratedProtocolMessageType('IMFilePullDataReq', (_message.Message,), dict(
  DESCRIPTOR = _IMFILEPULLDATAREQ,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFilePullDataReq)
  ))
_sym_db.RegisterMessage(IMFilePullDataReq)

IMFilePullDataRsp = _reflection.GeneratedProtocolMessageType('IMFilePullDataRsp', (_message.Message,), dict(
  DESCRIPTOR = _IMFILEPULLDATARSP,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFilePullDataRsp)
  ))
_sym_db.RegisterMessage(IMFilePullDataRsp)

IMFileReq = _reflection.GeneratedProtocolMessageType('IMFileReq', (_message.Message,), dict(
  DESCRIPTOR = _IMFILEREQ,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileReq)
  ))
_sym_db.RegisterMessage(IMFileReq)

IMFileRsp = _reflection.GeneratedProtocolMessageType('IMFileRsp', (_message.Message,), dict(
  DESCRIPTOR = _IMFILERSP,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileRsp)
  ))
_sym_db.RegisterMessage(IMFileRsp)

IMFileNotify = _reflection.GeneratedProtocolMessageType('IMFileNotify', (_message.Message,), dict(
  DESCRIPTOR = _IMFILENOTIFY,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileNotify)
  ))
_sym_db.RegisterMessage(IMFileNotify)

IMFileHasOfflineReq = _reflection.GeneratedProtocolMessageType('IMFileHasOfflineReq', (_message.Message,), dict(
  DESCRIPTOR = _IMFILEHASOFFLINEREQ,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileHasOfflineReq)
  ))
_sym_db.RegisterMessage(IMFileHasOfflineReq)

IMFileHasOfflineRsp = _reflection.GeneratedProtocolMessageType('IMFileHasOfflineRsp', (_message.Message,), dict(
  DESCRIPTOR = _IMFILEHASOFFLINERSP,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileHasOfflineRsp)
  ))
_sym_db.RegisterMessage(IMFileHasOfflineRsp)

IMFileAddOfflineReq = _reflection.GeneratedProtocolMessageType('IMFileAddOfflineReq', (_message.Message,), dict(
  DESCRIPTOR = _IMFILEADDOFFLINEREQ,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileAddOfflineReq)
  ))
_sym_db.RegisterMessage(IMFileAddOfflineReq)

IMFileDelOfflineReq = _reflection.GeneratedProtocolMessageType('IMFileDelOfflineReq', (_message.Message,), dict(
  DESCRIPTOR = _IMFILEDELOFFLINEREQ,
  __module__ = 'IM.File_pb2'
  # @@protoc_insertion_point(class_scope:IM.File.IMFileDelOfflineReq)
  ))
_sym_db.RegisterMessage(IMFileDelOfflineReq)


DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), _b('\n\027com.mogujie.tt.protobufH\003'))
# @@protoc_insertion_point(module_scope)
