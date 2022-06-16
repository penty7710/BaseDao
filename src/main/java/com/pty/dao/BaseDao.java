package com.pty.dao;

import com.pty.pojo.Student;
import com.pty.util.DBUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : pety
 * @date : 2022/6/15 20:43
 */
public class BaseDao {

    /**
     * 通用的保存方法
     * @param object 需要的保存对象
     * @return
     */
    public static boolean save(Object object) throws Exception {
        boolean flag = false;
        String sql = createInsertSqlByObject(object);
        Connection connection = null;
        Statement st = null;
        try{
            connection = DBUtil.getConnection();
            st = connection.createStatement();
            flag = st.executeUpdate(sql)>0;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 通用的删除方法
     * @param object
     * @return
     */
    public static boolean delete(Object object) throws Exception {
        boolean flag = false;
        String sql = createDeleteSql(object);
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            flag = st.executeUpdate(sql) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 通用的更新方法
     * @param object
     * @return
     */
    public static boolean update(Object object) throws Exception {
        boolean flag = false;
        String sql = createUpdateSqlByObject(object);
        Connection conn = null;
        Statement st = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            flag = st.executeUpdate(sql) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 根据ID查找对象
     * @param object    需要查找的对象
     * @return
     * @throws Exception
     */
    public static Object selectById(Object object) throws Exception {
        String sql  = createSelectByIdSql(object);
        Object result = null;
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while(rs.next()) {
                result = createObjectByResultSet(object, rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 通用查询方法
     * @param object
     * @return
     */
    public static List selectAll(Object object){
        List list = new ArrayList();
        String sql = "select * from "+getTableName(object);
        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            connection = DBUtil.getConnection();
            st = connection.createStatement();
            //获取到结果集对象
            rs = st.executeQuery(sql);
            //遍历结果集对象
            while(rs.next()){
                //将结果集封装为对象并添加到list集合中去。
                list.add((createObjectByResultSet(object,rs)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 通过结果集对象以及参数创建最终结果并返回值
     * @param object
     * @param rs
     * @return
     */
    private static Object createObjectByResultSet(Object object, ResultSet rs) throws Exception {
        Class clazz = object.getClass();
        //创建一个对象
        Object result = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            //得到属性的set方法名
            String methodName = getMethodNameByField("set",field);
            Method method = clazz.getDeclaredMethod(methodName);
            //调用set方法为属性设置值，参数从结果集中获取
            method.invoke(result,rs.getObject(getColumnName(field.getName())));
        }
        return result;
    }

    /**
     * 根据对象构建根据id查找的sql语句
     * @param object
     * @return
     */
    private static String createSelectByIdSql(Object object) throws Exception {
        Map map = getSetSqlValue(object);
        String sql = "select * from "+getTableName(object)+" where "+map.get("pk");
        return  sql;
    }

    /**
     * 根据对象构建出更新语句
     * @param object
     * @return
     */
    private static String createUpdateSqlByObject(Object object) throws Exception {
        //update t_student set stu_name='tom',major='金融',sex='男',age=19,remark='长沙' where stu_id=29
        Map result = getSetSqlValue(object);
        String sql = "update "+getTableName(object)+" set "+result.get("values")+" where "+result.get("pk");
        return sql;
    }

    /**
     * 根据对象构建删除sql语句
     * @param object
     * @return
     */
    private static String createDeleteSql(Object object) throws Exception {
        Map map = getSetSqlValue(object);
        //delete from t_student where sid = 1
        String sql = "delete from"+getTableName(object) + " where " + map.get("pk");
        return sql;
    }

    /**
     * 根据对象获取set参数字符串和主键字符串
     * @param object
     * @return
     */
    private static Map getSetSqlValue(Object object) throws Exception {
        Map map = new HashMap();
        String set = "";
        String pk = "";
        Class clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for(int i=0;i<fields.length;i++){
            String methodName = getMethodNameByField("get", fields[i]);
            Method method = clazz.getDeclaredMethod(methodName);
            Object result = method.invoke(object);
            if(i==0){
                pk = getColumnName(fields[i].getName())+"="+result;
            }else{
                set += getColumnName(fields[i].getName())+"=";
                switch (fields[i].getType().getSimpleName()){
                    case "Integer":
                    case "Float":
                        set+=result;
                        break;
                    default:
                        set+= "'"+result+"'";
                        break;
                }
                set+=",";
            }
        }
        map.put("values",set.substring(0,set.length()-1));
        map.put("pk",pk);
        return map;
    }

    /**
     * 通过列名获取对应列名
     * @param name
     * @return
     */
    private static String getColumnName(String name) {
        //stuName -> stu_name
        String result = "";
        for(int i=0;i<name.length();i++){
            char ch = name.charAt(i);
            if(Character.isUpperCase(ch)){
                result +="_";
            }
            result +=ch;
        }
        return result.toLowerCase();
    }

    /**
     * 根据对象构建插入的sql语句
     * @param object
     * @return
     */
    private static String createInsertSqlByObject(Object object) throws Exception {
        //insert into  t_student values(1,'jack','计算机','男',19,'长沙')
        String sql = "insert into "+getTableName(object)+"values";
        sql += getValuesByObject(object);
        return sql;
    }

    /**
     * 根据对象获取查询values语句片段
     * @param object
     * @return
     */
    private static String getValuesByObject(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String value = "(";
        Class clazz =  object.getClass();
        //得到所有属性
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            //构建出属性的get方法的方法名
            String methodName = getMethodNameByField("get",field);
            //获取到每个属性的get方法
            Method method = clazz.getDeclaredMethod(methodName);
            //得到get方法的返回值
            Object result = method.invoke(object);
            //得到属性的类型
            switch (field.getType().getSimpleName()){
                case "Integer":
                case "Float":
                    value+=result;
                    break;
                default:
                    value+= "'"+result+"'";
                    break;
            }
            value+=",";
        }
        //去掉最后一个逗号
        value = value.substring(0,value.length()-1);
        value += ")";
        return value;
    }

    /**
     * 根据字段名获取方法名
     * @param suff 前缀
     * @param field 字段
     * @return
     */
    private static String getMethodNameByField(String suff, Field field) {
        String methodName = suff;
        String name = field.getName();
        methodName += name.substring(0,1).toUpperCase()+name.substring(1);
        return methodName;
    }

    /**
     * 根据对象获取表的名字
     * 表名必须符合  t_实体类名
     * @param object
     * @return
     */
    private static String getTableName(Object object) {
        String tableName = "t_";
        //通过反射获取到类对象
        Class clazz = object.getClass();
        //获取类对象的实体类名并拼接字符串
        tableName += clazz.getSimpleName().toLowerCase();
        return tableName;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(createInsertSqlByObject(new Student(1,"jack","计算机","男",19,"长沙")));
        System.out.println(createUpdateSqlByObject(new Student(29,"tom","计算机","男",19,"长沙")));
        System.out.println(createSelectByIdSql(new Student(1, null,null, null, null, null)));
    }
}
