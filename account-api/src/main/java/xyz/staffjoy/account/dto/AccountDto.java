package xyz.staffjoy.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.common.validation.PhoneNumber;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:21 2019/12/24
 *
 */


/*
@Data ：@Data注解中包含了get，set和toString，所以我们直接在实体类中是@Data注解就可以免了再去手动创建这步骤了
@AllArgsConstructor ：生成一个有参构造函数
@NoArgsConstructor ：生成一个无参的构造函数
@Builder : 在设计数据实体时，对外保持private setter，而对属性的赋值采用Builder的方式,链式风格优雅地创建对象

class People加上了@Builder和@Data注解后，多了一个静态内部类PeopleBuilder，People调用静态方法builder生成PeopleBuilder对象，
PeopleBuilder对象可以使用".属性名(属性值)"的方式进行属性设置，再调用build()方法就生成了People对象，
并且如果两个People对象的属性如果相同，就会认为这两个对象相等，即重写了hashCode和equls方法

可以利用Javap、cfr进行反编译该字节码；
这里就直接在Intellij IDEA下，查看反编译的文件People.class；
可以看到，生成的有：
    Getter和Setter方法；
    访问类型是private无参构造方法，访问类型为default的全部参数的构造方法；
    重写hashCode、equals、toString方法，则People可以做为Map的key；
    访问类型为public的静态方法builder，返回的是People.PeopleBuilder对象，非单例；
    访问类型为public的静态内部类PeopleBuilder，该类主要有build方法，返回类型是People；
    最后还有个canEqual方法，判断是否与People同类型。


 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDto {
    @NotBlank
    private String id;
    private String name;
    @Email(message = "Invalid email")
    private String email;
    private boolean confirmedAndActive;
    @NotNull
    private Instant memberSince;
    private boolean support;
    @PhoneNumber
    private String phoneNumber;
    @NotEmpty
    private String photoUrl;
}
