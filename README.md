# NewBigdataTextbook

这是教材文字&图片仓库

## 文件存储格式规范
图片请放在`pic/chapter/`下，如`pic/1/`
图片名请严格遵循“chapter-该章节第几张图片[空格\][图片标题\]”，如“1-7 大数据4V.jpg”，图片png or jpg or 其他随意，能读出来就行。如大家协作写作时遇见第几张图重名，例如：1-2 小明.jpg 和 1-2 小红.jpg，先搁置图片前面的数字重合的问题，等到最后定稿时统一修改图片标题即可。

各小节md请放在`txt/chapter/`下，如`txt/1/`
各小节md文件名请严格遵循“chapter.该章节第几小节[空格\][小节标题\]”，如“1.1 大数据简介.md”
若章节小节内还需划分小节，如1.2.4，请合并写到1.2中。

## 写作规范

形如2.2小节请使用二级标题， 例如：

## 2.2 xxx

形如2.2.1小节请使用三级标题，例如：

### 2.2.1 xxx

如需再分小节请使用加粗+（x），例如：

**（1）xxx**

如需描述文件结构请使用引用代码完成，例如：
```
test_files
    |
    |--maths
    |   |--maths_students.xml    
    |   |--maths_scores.txt    
    |
    |--all
        |--teachers
            |
            |--list.xml        
    
```

如需描述流程步骤，请使用：

1. xxxxx
2. xxx
3. xxxx
4. xx

如需列点，但各小点间无序，请使用：

* aaaa
* bbb


如需提示/指定操作指令的主机，请使用：

_只在m1上执行_

当需要引用指令时，若其不长，则非必要不分行。例如：`start-dfs.sh`。

当引用配置文件或者进行一连串操作时，在必要之处请给出注释。




**github上写作指南**

https://zhuanlan.zhihu.com/p/3048211224

**markdown语法介绍**

https://blog.csdn.net/2301_77569009/article/details/137957203

**如何插入图片并缩放居中,首行缩进**

见`test.md`
