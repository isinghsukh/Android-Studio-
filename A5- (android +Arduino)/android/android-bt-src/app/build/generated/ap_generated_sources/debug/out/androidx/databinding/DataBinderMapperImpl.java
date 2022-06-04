package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new edu.uw.ee523.btdemo.DataBinderMapperImpl());
  }
}
