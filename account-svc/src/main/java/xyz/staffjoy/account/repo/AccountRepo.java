package xyz.staffjoy.account.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.staffjoy.account.model.Account;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:55 2019/12/24
 *
 */

/*

　1、在@Query注解中编写JPQL实现DELETE和UPDATE操作的时候必须加上@modifying注解，以通知Spring Data 这是一个DELETE或UPDATE操作。
　2、UPDATE或者DELETE操作需要使用事务，此时需要 定义Service层，在Service层的方法上添加事务操作。
　3、注意JPQL不支持INSERT操作。　　

---

@Param注解单一属性 ： @param(“userName”) String name
@Param注解JavaBean对象 ：@Param("user") User user
当你使用了使用@Param注解来声明参数时，如果使用 #{} 或 ${} 的方式都可以。
不使用@Param注解时，参数只能有一个，并且是Javabean。在SQL语句里可以引用JavaBean的属性，而且只能引用JavaBean的属性。
    // 这里id是user的属性
    @Select("SELECT * from Table where id = ${id}")
    Enchashment selectUserById(User user);

---
当参数是一个JavaBean时，如果不用@Param且sql里获取变量用#{}，如
@Select("SELECT id,USERNAME,uname from uk_user where del = 0 LIMIT #{pageParam.pageStart}, #{pageParam.pageSize}")List<UserVo> queryUserPageList(PageParam pageParam);
运行时会出现错误

但如果sql里获取变量用${}，则可以直接引用JavaBean的属性，而且只能引用JavaBean的属性，如
@Select("SELECT id,USERNAME,uname from uk_user where del = 0 LIMIT ${pageStart}") List<UserVo> queryUserPageList(PageParam pageParam);

如果想用#{}在sql里获取变量，则必须要加上@Param，如
@Select("SELECT id,USERNAME,uname from uk_user where del = 0 LIMIT #{pageParam.pageStart}, #{pageParam.pageSize}")List<UserVo> queryUserPageList(@Param("pageParam") PageParam pageParam);

或者不用@Param，引用JavaBean的属性，如
@Select("SELECT id,USERNAME,uname from uk_user where del = 0 AND username like CONCAT('%',#{userName},'%')")List<UserVo> queryUserPageList(UserQo userQo);

---

JpaRepository支持接口规范方法名查询。意思是如果在接口中定义的查询方法符合它的命名规则，就可以不用写实现

 */

@Repository
public interface AccountRepo extends JpaRepository<Account, String> {

    Account findAccountById(String id);

    Account findAccountByEmail(String email);

    Account findAccountByPhoneNumber(String phoneNumber);

    @Modifying(clearAutomatically = true)
    @Query("update Account account set account.email = :email, account.confirmedAndActive = true where account.id = :id")
    @Transactional
    int updateEmailAndActivateById(@Param("email") String email, @Param("id") String id);

}
