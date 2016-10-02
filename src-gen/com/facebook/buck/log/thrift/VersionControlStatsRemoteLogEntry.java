/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.facebook.buck.log.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
public class VersionControlStatsRemoteLogEntry implements org.apache.thrift.TBase<VersionControlStatsRemoteLogEntry, VersionControlStatsRemoteLogEntry._Fields>, java.io.Serializable, Cloneable, Comparable<VersionControlStatsRemoteLogEntry> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("VersionControlStatsRemoteLogEntry");

  private static final org.apache.thrift.protocol.TField CURRENT_REVISION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("currentRevisionId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField BASE_BOOKMARKS_FIELD_DESC = new org.apache.thrift.protocol.TField("baseBookmarks", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField PATHS_CHANGED_FIELD_DESC = new org.apache.thrift.protocol.TField("pathsChanged", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField PATHS_CHANGED_SAMPLED_FIELD_DESC = new org.apache.thrift.protocol.TField("pathsChangedSampled", org.apache.thrift.protocol.TType.BOOL, (short)4);
  private static final org.apache.thrift.protocol.TField UNSAMPLED_PATHS_CHANGED_COUNT_FIELD_DESC = new org.apache.thrift.protocol.TField("unsampledPathsChangedCount", org.apache.thrift.protocol.TType.I32, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new VersionControlStatsRemoteLogEntryStandardSchemeFactory());
    schemes.put(TupleScheme.class, new VersionControlStatsRemoteLogEntryTupleSchemeFactory());
  }

  public String currentRevisionId; // optional
  public List<String> baseBookmarks; // optional
  public List<String> pathsChanged; // optional
  public boolean pathsChangedSampled; // optional
  public int unsampledPathsChangedCount; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CURRENT_REVISION_ID((short)1, "currentRevisionId"),
    BASE_BOOKMARKS((short)2, "baseBookmarks"),
    PATHS_CHANGED((short)3, "pathsChanged"),
    PATHS_CHANGED_SAMPLED((short)4, "pathsChangedSampled"),
    UNSAMPLED_PATHS_CHANGED_COUNT((short)5, "unsampledPathsChangedCount");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // CURRENT_REVISION_ID
          return CURRENT_REVISION_ID;
        case 2: // BASE_BOOKMARKS
          return BASE_BOOKMARKS;
        case 3: // PATHS_CHANGED
          return PATHS_CHANGED;
        case 4: // PATHS_CHANGED_SAMPLED
          return PATHS_CHANGED_SAMPLED;
        case 5: // UNSAMPLED_PATHS_CHANGED_COUNT
          return UNSAMPLED_PATHS_CHANGED_COUNT;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __PATHSCHANGEDSAMPLED_ISSET_ID = 0;
  private static final int __UNSAMPLEDPATHSCHANGEDCOUNT_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.CURRENT_REVISION_ID,_Fields.BASE_BOOKMARKS,_Fields.PATHS_CHANGED,_Fields.PATHS_CHANGED_SAMPLED,_Fields.UNSAMPLED_PATHS_CHANGED_COUNT};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CURRENT_REVISION_ID, new org.apache.thrift.meta_data.FieldMetaData("currentRevisionId", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.BASE_BOOKMARKS, new org.apache.thrift.meta_data.FieldMetaData("baseBookmarks", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.PATHS_CHANGED, new org.apache.thrift.meta_data.FieldMetaData("pathsChanged", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.PATHS_CHANGED_SAMPLED, new org.apache.thrift.meta_data.FieldMetaData("pathsChangedSampled", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.UNSAMPLED_PATHS_CHANGED_COUNT, new org.apache.thrift.meta_data.FieldMetaData("unsampledPathsChangedCount", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(VersionControlStatsRemoteLogEntry.class, metaDataMap);
  }

  public VersionControlStatsRemoteLogEntry() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public VersionControlStatsRemoteLogEntry(VersionControlStatsRemoteLogEntry other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetCurrentRevisionId()) {
      this.currentRevisionId = other.currentRevisionId;
    }
    if (other.isSetBaseBookmarks()) {
      List<String> __this__baseBookmarks = new ArrayList<String>(other.baseBookmarks);
      this.baseBookmarks = __this__baseBookmarks;
    }
    if (other.isSetPathsChanged()) {
      List<String> __this__pathsChanged = new ArrayList<String>(other.pathsChanged);
      this.pathsChanged = __this__pathsChanged;
    }
    this.pathsChangedSampled = other.pathsChangedSampled;
    this.unsampledPathsChangedCount = other.unsampledPathsChangedCount;
  }

  public VersionControlStatsRemoteLogEntry deepCopy() {
    return new VersionControlStatsRemoteLogEntry(this);
  }

  @Override
  public void clear() {
    this.currentRevisionId = null;
    this.baseBookmarks = null;
    this.pathsChanged = null;
    setPathsChangedSampledIsSet(false);
    this.pathsChangedSampled = false;
    setUnsampledPathsChangedCountIsSet(false);
    this.unsampledPathsChangedCount = 0;
  }

  public String getCurrentRevisionId() {
    return this.currentRevisionId;
  }

  public VersionControlStatsRemoteLogEntry setCurrentRevisionId(String currentRevisionId) {
    this.currentRevisionId = currentRevisionId;
    return this;
  }

  public void unsetCurrentRevisionId() {
    this.currentRevisionId = null;
  }

  /** Returns true if field currentRevisionId is set (has been assigned a value) and false otherwise */
  public boolean isSetCurrentRevisionId() {
    return this.currentRevisionId != null;
  }

  public void setCurrentRevisionIdIsSet(boolean value) {
    if (!value) {
      this.currentRevisionId = null;
    }
  }

  public int getBaseBookmarksSize() {
    return (this.baseBookmarks == null) ? 0 : this.baseBookmarks.size();
  }

  public java.util.Iterator<String> getBaseBookmarksIterator() {
    return (this.baseBookmarks == null) ? null : this.baseBookmarks.iterator();
  }

  public void addToBaseBookmarks(String elem) {
    if (this.baseBookmarks == null) {
      this.baseBookmarks = new ArrayList<String>();
    }
    this.baseBookmarks.add(elem);
  }

  public List<String> getBaseBookmarks() {
    return this.baseBookmarks;
  }

  public VersionControlStatsRemoteLogEntry setBaseBookmarks(List<String> baseBookmarks) {
    this.baseBookmarks = baseBookmarks;
    return this;
  }

  public void unsetBaseBookmarks() {
    this.baseBookmarks = null;
  }

  /** Returns true if field baseBookmarks is set (has been assigned a value) and false otherwise */
  public boolean isSetBaseBookmarks() {
    return this.baseBookmarks != null;
  }

  public void setBaseBookmarksIsSet(boolean value) {
    if (!value) {
      this.baseBookmarks = null;
    }
  }

  public int getPathsChangedSize() {
    return (this.pathsChanged == null) ? 0 : this.pathsChanged.size();
  }

  public java.util.Iterator<String> getPathsChangedIterator() {
    return (this.pathsChanged == null) ? null : this.pathsChanged.iterator();
  }

  public void addToPathsChanged(String elem) {
    if (this.pathsChanged == null) {
      this.pathsChanged = new ArrayList<String>();
    }
    this.pathsChanged.add(elem);
  }

  public List<String> getPathsChanged() {
    return this.pathsChanged;
  }

  public VersionControlStatsRemoteLogEntry setPathsChanged(List<String> pathsChanged) {
    this.pathsChanged = pathsChanged;
    return this;
  }

  public void unsetPathsChanged() {
    this.pathsChanged = null;
  }

  /** Returns true if field pathsChanged is set (has been assigned a value) and false otherwise */
  public boolean isSetPathsChanged() {
    return this.pathsChanged != null;
  }

  public void setPathsChangedIsSet(boolean value) {
    if (!value) {
      this.pathsChanged = null;
    }
  }

  public boolean isPathsChangedSampled() {
    return this.pathsChangedSampled;
  }

  public VersionControlStatsRemoteLogEntry setPathsChangedSampled(boolean pathsChangedSampled) {
    this.pathsChangedSampled = pathsChangedSampled;
    setPathsChangedSampledIsSet(true);
    return this;
  }

  public void unsetPathsChangedSampled() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PATHSCHANGEDSAMPLED_ISSET_ID);
  }

  /** Returns true if field pathsChangedSampled is set (has been assigned a value) and false otherwise */
  public boolean isSetPathsChangedSampled() {
    return EncodingUtils.testBit(__isset_bitfield, __PATHSCHANGEDSAMPLED_ISSET_ID);
  }

  public void setPathsChangedSampledIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PATHSCHANGEDSAMPLED_ISSET_ID, value);
  }

  public int getUnsampledPathsChangedCount() {
    return this.unsampledPathsChangedCount;
  }

  public VersionControlStatsRemoteLogEntry setUnsampledPathsChangedCount(int unsampledPathsChangedCount) {
    this.unsampledPathsChangedCount = unsampledPathsChangedCount;
    setUnsampledPathsChangedCountIsSet(true);
    return this;
  }

  public void unsetUnsampledPathsChangedCount() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __UNSAMPLEDPATHSCHANGEDCOUNT_ISSET_ID);
  }

  /** Returns true if field unsampledPathsChangedCount is set (has been assigned a value) and false otherwise */
  public boolean isSetUnsampledPathsChangedCount() {
    return EncodingUtils.testBit(__isset_bitfield, __UNSAMPLEDPATHSCHANGEDCOUNT_ISSET_ID);
  }

  public void setUnsampledPathsChangedCountIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __UNSAMPLEDPATHSCHANGEDCOUNT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CURRENT_REVISION_ID:
      if (value == null) {
        unsetCurrentRevisionId();
      } else {
        setCurrentRevisionId((String)value);
      }
      break;

    case BASE_BOOKMARKS:
      if (value == null) {
        unsetBaseBookmarks();
      } else {
        setBaseBookmarks((List<String>)value);
      }
      break;

    case PATHS_CHANGED:
      if (value == null) {
        unsetPathsChanged();
      } else {
        setPathsChanged((List<String>)value);
      }
      break;

    case PATHS_CHANGED_SAMPLED:
      if (value == null) {
        unsetPathsChangedSampled();
      } else {
        setPathsChangedSampled((Boolean)value);
      }
      break;

    case UNSAMPLED_PATHS_CHANGED_COUNT:
      if (value == null) {
        unsetUnsampledPathsChangedCount();
      } else {
        setUnsampledPathsChangedCount((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CURRENT_REVISION_ID:
      return getCurrentRevisionId();

    case BASE_BOOKMARKS:
      return getBaseBookmarks();

    case PATHS_CHANGED:
      return getPathsChanged();

    case PATHS_CHANGED_SAMPLED:
      return isPathsChangedSampled();

    case UNSAMPLED_PATHS_CHANGED_COUNT:
      return getUnsampledPathsChangedCount();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CURRENT_REVISION_ID:
      return isSetCurrentRevisionId();
    case BASE_BOOKMARKS:
      return isSetBaseBookmarks();
    case PATHS_CHANGED:
      return isSetPathsChanged();
    case PATHS_CHANGED_SAMPLED:
      return isSetPathsChangedSampled();
    case UNSAMPLED_PATHS_CHANGED_COUNT:
      return isSetUnsampledPathsChangedCount();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof VersionControlStatsRemoteLogEntry)
      return this.equals((VersionControlStatsRemoteLogEntry)that);
    return false;
  }

  public boolean equals(VersionControlStatsRemoteLogEntry that) {
    if (that == null)
      return false;

    boolean this_present_currentRevisionId = true && this.isSetCurrentRevisionId();
    boolean that_present_currentRevisionId = true && that.isSetCurrentRevisionId();
    if (this_present_currentRevisionId || that_present_currentRevisionId) {
      if (!(this_present_currentRevisionId && that_present_currentRevisionId))
        return false;
      if (!this.currentRevisionId.equals(that.currentRevisionId))
        return false;
    }

    boolean this_present_baseBookmarks = true && this.isSetBaseBookmarks();
    boolean that_present_baseBookmarks = true && that.isSetBaseBookmarks();
    if (this_present_baseBookmarks || that_present_baseBookmarks) {
      if (!(this_present_baseBookmarks && that_present_baseBookmarks))
        return false;
      if (!this.baseBookmarks.equals(that.baseBookmarks))
        return false;
    }

    boolean this_present_pathsChanged = true && this.isSetPathsChanged();
    boolean that_present_pathsChanged = true && that.isSetPathsChanged();
    if (this_present_pathsChanged || that_present_pathsChanged) {
      if (!(this_present_pathsChanged && that_present_pathsChanged))
        return false;
      if (!this.pathsChanged.equals(that.pathsChanged))
        return false;
    }

    boolean this_present_pathsChangedSampled = true && this.isSetPathsChangedSampled();
    boolean that_present_pathsChangedSampled = true && that.isSetPathsChangedSampled();
    if (this_present_pathsChangedSampled || that_present_pathsChangedSampled) {
      if (!(this_present_pathsChangedSampled && that_present_pathsChangedSampled))
        return false;
      if (this.pathsChangedSampled != that.pathsChangedSampled)
        return false;
    }

    boolean this_present_unsampledPathsChangedCount = true && this.isSetUnsampledPathsChangedCount();
    boolean that_present_unsampledPathsChangedCount = true && that.isSetUnsampledPathsChangedCount();
    if (this_present_unsampledPathsChangedCount || that_present_unsampledPathsChangedCount) {
      if (!(this_present_unsampledPathsChangedCount && that_present_unsampledPathsChangedCount))
        return false;
      if (this.unsampledPathsChangedCount != that.unsampledPathsChangedCount)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_currentRevisionId = true && (isSetCurrentRevisionId());
    list.add(present_currentRevisionId);
    if (present_currentRevisionId)
      list.add(currentRevisionId);

    boolean present_baseBookmarks = true && (isSetBaseBookmarks());
    list.add(present_baseBookmarks);
    if (present_baseBookmarks)
      list.add(baseBookmarks);

    boolean present_pathsChanged = true && (isSetPathsChanged());
    list.add(present_pathsChanged);
    if (present_pathsChanged)
      list.add(pathsChanged);

    boolean present_pathsChangedSampled = true && (isSetPathsChangedSampled());
    list.add(present_pathsChangedSampled);
    if (present_pathsChangedSampled)
      list.add(pathsChangedSampled);

    boolean present_unsampledPathsChangedCount = true && (isSetUnsampledPathsChangedCount());
    list.add(present_unsampledPathsChangedCount);
    if (present_unsampledPathsChangedCount)
      list.add(unsampledPathsChangedCount);

    return list.hashCode();
  }

  @Override
  public int compareTo(VersionControlStatsRemoteLogEntry other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetCurrentRevisionId()).compareTo(other.isSetCurrentRevisionId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCurrentRevisionId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.currentRevisionId, other.currentRevisionId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetBaseBookmarks()).compareTo(other.isSetBaseBookmarks());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBaseBookmarks()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.baseBookmarks, other.baseBookmarks);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPathsChanged()).compareTo(other.isSetPathsChanged());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPathsChanged()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.pathsChanged, other.pathsChanged);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPathsChangedSampled()).compareTo(other.isSetPathsChangedSampled());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPathsChangedSampled()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.pathsChangedSampled, other.pathsChangedSampled);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetUnsampledPathsChangedCount()).compareTo(other.isSetUnsampledPathsChangedCount());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUnsampledPathsChangedCount()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.unsampledPathsChangedCount, other.unsampledPathsChangedCount);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("VersionControlStatsRemoteLogEntry(");
    boolean first = true;

    if (isSetCurrentRevisionId()) {
      sb.append("currentRevisionId:");
      if (this.currentRevisionId == null) {
        sb.append("null");
      } else {
        sb.append(this.currentRevisionId);
      }
      first = false;
    }
    if (isSetBaseBookmarks()) {
      if (!first) sb.append(", ");
      sb.append("baseBookmarks:");
      if (this.baseBookmarks == null) {
        sb.append("null");
      } else {
        sb.append(this.baseBookmarks);
      }
      first = false;
    }
    if (isSetPathsChanged()) {
      if (!first) sb.append(", ");
      sb.append("pathsChanged:");
      if (this.pathsChanged == null) {
        sb.append("null");
      } else {
        sb.append(this.pathsChanged);
      }
      first = false;
    }
    if (isSetPathsChangedSampled()) {
      if (!first) sb.append(", ");
      sb.append("pathsChangedSampled:");
      sb.append(this.pathsChangedSampled);
      first = false;
    }
    if (isSetUnsampledPathsChangedCount()) {
      if (!first) sb.append(", ");
      sb.append("unsampledPathsChangedCount:");
      sb.append(this.unsampledPathsChangedCount);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class VersionControlStatsRemoteLogEntryStandardSchemeFactory implements SchemeFactory {
    public VersionControlStatsRemoteLogEntryStandardScheme getScheme() {
      return new VersionControlStatsRemoteLogEntryStandardScheme();
    }
  }

  private static class VersionControlStatsRemoteLogEntryStandardScheme extends StandardScheme<VersionControlStatsRemoteLogEntry> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, VersionControlStatsRemoteLogEntry struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CURRENT_REVISION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.currentRevisionId = iprot.readString();
              struct.setCurrentRevisionIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // BASE_BOOKMARKS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.baseBookmarks = new ArrayList<String>(_list0.size);
                String _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = iprot.readString();
                  struct.baseBookmarks.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setBaseBookmarksIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PATHS_CHANGED
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list3 = iprot.readListBegin();
                struct.pathsChanged = new ArrayList<String>(_list3.size);
                String _elem4;
                for (int _i5 = 0; _i5 < _list3.size; ++_i5)
                {
                  _elem4 = iprot.readString();
                  struct.pathsChanged.add(_elem4);
                }
                iprot.readListEnd();
              }
              struct.setPathsChangedIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // PATHS_CHANGED_SAMPLED
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.pathsChangedSampled = iprot.readBool();
              struct.setPathsChangedSampledIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // UNSAMPLED_PATHS_CHANGED_COUNT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.unsampledPathsChangedCount = iprot.readI32();
              struct.setUnsampledPathsChangedCountIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, VersionControlStatsRemoteLogEntry struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.currentRevisionId != null) {
        if (struct.isSetCurrentRevisionId()) {
          oprot.writeFieldBegin(CURRENT_REVISION_ID_FIELD_DESC);
          oprot.writeString(struct.currentRevisionId);
          oprot.writeFieldEnd();
        }
      }
      if (struct.baseBookmarks != null) {
        if (struct.isSetBaseBookmarks()) {
          oprot.writeFieldBegin(BASE_BOOKMARKS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.baseBookmarks.size()));
            for (String _iter6 : struct.baseBookmarks)
            {
              oprot.writeString(_iter6);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.pathsChanged != null) {
        if (struct.isSetPathsChanged()) {
          oprot.writeFieldBegin(PATHS_CHANGED_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.pathsChanged.size()));
            for (String _iter7 : struct.pathsChanged)
            {
              oprot.writeString(_iter7);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetPathsChangedSampled()) {
        oprot.writeFieldBegin(PATHS_CHANGED_SAMPLED_FIELD_DESC);
        oprot.writeBool(struct.pathsChangedSampled);
        oprot.writeFieldEnd();
      }
      if (struct.isSetUnsampledPathsChangedCount()) {
        oprot.writeFieldBegin(UNSAMPLED_PATHS_CHANGED_COUNT_FIELD_DESC);
        oprot.writeI32(struct.unsampledPathsChangedCount);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class VersionControlStatsRemoteLogEntryTupleSchemeFactory implements SchemeFactory {
    public VersionControlStatsRemoteLogEntryTupleScheme getScheme() {
      return new VersionControlStatsRemoteLogEntryTupleScheme();
    }
  }

  private static class VersionControlStatsRemoteLogEntryTupleScheme extends TupleScheme<VersionControlStatsRemoteLogEntry> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, VersionControlStatsRemoteLogEntry struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetCurrentRevisionId()) {
        optionals.set(0);
      }
      if (struct.isSetBaseBookmarks()) {
        optionals.set(1);
      }
      if (struct.isSetPathsChanged()) {
        optionals.set(2);
      }
      if (struct.isSetPathsChangedSampled()) {
        optionals.set(3);
      }
      if (struct.isSetUnsampledPathsChangedCount()) {
        optionals.set(4);
      }
      oprot.writeBitSet(optionals, 5);
      if (struct.isSetCurrentRevisionId()) {
        oprot.writeString(struct.currentRevisionId);
      }
      if (struct.isSetBaseBookmarks()) {
        {
          oprot.writeI32(struct.baseBookmarks.size());
          for (String _iter8 : struct.baseBookmarks)
          {
            oprot.writeString(_iter8);
          }
        }
      }
      if (struct.isSetPathsChanged()) {
        {
          oprot.writeI32(struct.pathsChanged.size());
          for (String _iter9 : struct.pathsChanged)
          {
            oprot.writeString(_iter9);
          }
        }
      }
      if (struct.isSetPathsChangedSampled()) {
        oprot.writeBool(struct.pathsChangedSampled);
      }
      if (struct.isSetUnsampledPathsChangedCount()) {
        oprot.writeI32(struct.unsampledPathsChangedCount);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, VersionControlStatsRemoteLogEntry struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(5);
      if (incoming.get(0)) {
        struct.currentRevisionId = iprot.readString();
        struct.setCurrentRevisionIdIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list10 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.baseBookmarks = new ArrayList<String>(_list10.size);
          String _elem11;
          for (int _i12 = 0; _i12 < _list10.size; ++_i12)
          {
            _elem11 = iprot.readString();
            struct.baseBookmarks.add(_elem11);
          }
        }
        struct.setBaseBookmarksIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.pathsChanged = new ArrayList<String>(_list13.size);
          String _elem14;
          for (int _i15 = 0; _i15 < _list13.size; ++_i15)
          {
            _elem14 = iprot.readString();
            struct.pathsChanged.add(_elem14);
          }
        }
        struct.setPathsChangedIsSet(true);
      }
      if (incoming.get(3)) {
        struct.pathsChangedSampled = iprot.readBool();
        struct.setPathsChangedSampledIsSet(true);
      }
      if (incoming.get(4)) {
        struct.unsampledPathsChangedCount = iprot.readI32();
        struct.setUnsampledPathsChangedCountIsSet(true);
      }
    }
  }

}

