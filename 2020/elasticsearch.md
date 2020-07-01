
> 所有语句都是http请求，完整语句前均有 地址:端口，如http://192.168.8.83:9200  

## 简单查询 GET

- 美化查询结果 */?pretty*

- 统计文档数量  
GET /_count  
```json
{
  "query": {
    "match_all": {}
  }
}
```

> 以下查找需要存在索引megacrop，类型employee，插入参考下文
- 查找指定ID
GET /megacrop/employee/3

- 查找所有  
GET /megacrop/employee/_search

- 查找名为Smith的员工  
GET /megacrop/employee/_search?q=last_name:Smith

- DSL查询名为Smith的员工  
GET /megacrop/employee/_search
```json
{
  "query": {
    "match": {
      "last_name": "Smith"
    }
  }
}
```
> GET本身可以携带body，但有些服务器不接收GET的body，此时可以用POST来查询

- 过滤出年龄大于20岁，名为Smith的员工
> filtered已在es5.0后废弃  
GET /megacrop/employee/_search
```json
{
  "query": {
    "bool": {
      "filter": {
        "range": {
          "age": {"gt": 20}
        }
      },
      "must": {
        "match": {
          "last_name": "Smith"
        }
      }
    }
  }
}
```
- 全文检索
> 查询about字段与**cooking**相关的文档，返回结果中包含**_score**字段显示相关性
GET /megacrop/employee/_search
```json
{
  "query": {
    "match": {
      "about": "cooking"
    }
  }
}
```

- 匹配短语
> **match_phrase**表示匹配短语
GET /megacrop/employee/_search
```json
{
  "query": {
    "match_phrase": {
      "about": "love cooking"
    }
  }
}
```

- 高亮
GET megacrop/employee/_search
```json
{
  "query": {
    "match_phrase": {
      "about": "love cooking"
    }
  },
  "highlight": {
    "fields": {
      "about": {}
    }
  }
}
```
> 返回的结果中新增了highlight字段，并将在about中匹配的短语，用em标签包裹起来
```json
{
  "highlight": {
    "about": [
      "I <em>love</em> <em>cooking</em>!"
    ]
  }
}
```

- 存在（字段中存在workspaceId字段的doc）
```json
GET /tapd/tapd_bugs/_search
{
  "query": {
    "bool": {
      "must": {
        "exists": {
          "field": "workspaceId"
        }
      }
    }
  }
}
```

- 不存在（字段中不存在workspaceId字段的doc）
```json
GET /tapd/tapd_bugs/_search
{
  "query": {
    "bool": {
      "must_not": {
        "exists": {
          "field": "workspaceId"
        }
      }
    }
  }
}
```

## 聚合统计
- 平均值
GET /megacrop/employee/_search?size=0
```json
{
  "aggs": {
    "avg_age": {
      "avg": {
        "field": "age"
      }
    }
  }
}
```

> 简单聚合格式
```json
{
  "aggs | aggregations": {
    "<聚合结果名>": {
      "avg | max | min | sum | Cardinality | stats": {
        "field": "<聚合字段>"
      }
    }
  }
}
```





## 插入

- 往megacrop索引（数据库）的employee类型（数据表）里插入一条数据，员工ID为1  
PUT /megacrop/employee/1
```json 
{
  "first_name": "Jhon",
  "last_name": "Smith",
  "age": 25,
  "about": "I love cooking!",
  "interests": [
    "sports",
    "music"
  ]
}
```


## 集成Spring Boot

### 使用RestHighLevelClient

1. 配置pom，yml

```xml
<dependency>
  <groupId>org.elasticsearch</groupId>
  <artifactId>elasticsearch</artifactId>
  <version>6.2.4</version>
</dependency>
<dependency>
  <groupId>org.elasticsearch.client</groupId>
  <artifactId>elasticsearch-rest-high-level-client</artifactId>
  <version>6.2.4</version>
</dependency>
```
> [ClassNotFoundException](https://blog.csdn.net/qq_35726730/article/details/82049168)
elasticsearch和rest-high-level-client版本要一致，如果版本一致还出现了ClassNotFoundException，是因为pom里的确还有其他版本的elasticsearch，可能是从parent那里继承来的，重写

2. yml文件配置elasticsearch.host，配置RestHighLevelClient

```yml
elasticsearch.host: 192.168.8.83
```

```java
@Slf4j
@Configuration
public class RestClientConfiguration extends AbstractFactoryBean {

    @Value("${elasticsearch.host}")
    private String host;

    private RestHighLevelClient restHighLevelClient;

    @Override
    public void destroy() throws Exception {
        if (restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }

    @Override
    public Class<RestHighLevelClient> getObjectType() {
        return RestHighLevelClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    protected Object createInstance() throws Exception {
        try {
            // 如果有多个节点，构建多个HttpHost
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(host, 9200, "http")));
        } catch (Exception e) {
            log.error("{}", e);
        }
        return restHighLevelClient;
    }
}

```

3. 使用RestHighLevelClient访问接口

```java
@Repository
@Slf4j
public class EmployeeDao {

    private final String INDEX = "megacrop";
    private final String TYPE = "employee";

    @Autowired
    RestHighLevelClient client;


    public List<Employee> findAll() {
        List<Employee> result = new ArrayList<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(/*查询全部*/);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest(INDEX);
        searchRequest.types(TYPE);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse search = client.search(searchRequest);
            SearchHit[] hits = search.getHits().getHits();

            for (SearchHit hit : hits) {
                Map<String, Object> hitMap = hit.getSourceAsMap();
                log.info("es data = {}", JSON.toJSONString(hitMap));
                String firstName = (String) hitMap.get("first_name");
                String lastName = (String) hitMap.get("last_name");
                String about = (String) hitMap.get("about");
                List<String> interests = (List<String>) hitMap.get("interests");
                result.add(new Employee(null, firstName, lastName, about, interests));
            }
        } catch (IOException e) {
            log.error("{}", e);
        }
        return result;
    }
}
``` 

## 聚合
- 批量索引一批数据
```json
POST /cars/transactions/_bulk
{ "index": {}}
{ "price" : 10000, "color" : "red", "make" : "honda", "sold" : "2014-10-28" }
{ "index": {}}
{ "price" : 20000, "color" : "red", "make" : "honda", "sold" : "2014-11-05" }
{ "index": {}}
{ "price" : 30000, "color" : "green", "make" : "ford", "sold" : "2014-05-18" }
{ "index": {}}
{ "price" : 15000, "color" : "blue", "make" : "toyota", "sold" : "2014-07-02" }
{ "index": {}}
{ "price" : 12000, "color" : "green", "make" : "toyota", "sold" : "2014-08-19" }
{ "index": {}}
{ "price" : 20000, "color" : "red", "make" : "honda", "sold" : "2014-11-05" }
{ "index": {}}
{ "price" : 80000, "color" : "red", "make" : "bmw", "sold" : "2014-01-01" }
{ "index": {}}
{ "price" : 25000, "color" : "blue", "make" : "ford", "sold" : "2014-02-12" }
```

- terms桶，相当于group by进行分组
```json
GET /cars/transactions/_search
{
    "size" : 0,
    "aggs" : { 
        "popular_colors" : { 
            "terms" : { 
              "field" : "color"
            }
        }
    }
}
```

> 问题: Fielddata is disabled on text fields by default  
> 原因： es5.x之后对聚合操作需要的字段都使用fielddata缓存到内存了，默认关闭，开启需要
```json
PUT /index-name/_mapping/type-name
{
  "properties": {
    "property-name": {
      "type": "text",
      "fielddata": true
    }
  }
}
```

- 度量指标，相当于聚合函数
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "colors": {
      "terms": {
        "field": "color"
      },
      "aggs": {
        "avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  }
}
```

- 嵌套桶，在查到平均值的同时，仍能查到制造厂商
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "colors": {
      "terms": {
        "field": "color"
      },
      "aggs": {
        "avg_price": {
          "avg": {
            "field": "price"
          }
        },
        "make": {
          "terms": {
            "field": "make"
          }
        }
      }
    }
  }
}
```

- 桶 -> 度量 -> 桶 -> 度量
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "colors": {
      "terms": {
        "field": "color"
      },
      "aggs": {
        "avg_price": {
          "avg": {
            "field": "price"
          }
        },
        "make": {
          "terms": {
            "field": "make"
          },
          "aggs": {
            "min_price": {
              "min": {
                "field": "price"
              }
            },
            "max_price": {
              "max": {
                "field": "price"
              }
            }
          }
        }
      }
    }
  }
}
```

- 直方图
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "price": {
      "histogram": {
        "field": "price",
        "interval": 2000
      },
      "aggs": {
        "revenue": {
          "sum": {
            "field": "price"
          }
        }
      }
    }
  }
}
```

- 统计数据extended_stats
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "makes": {
      "terms": {
        "field": "make",
        "size": 10
      },
      "aggs": {
        "stats": {
          "extended_stats": {
            "field": "price"
          }
        }
      }
    }
  }
}
```

- 按时间分段
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "sales": {
      "date_histogram": {
        "field": "sold",
        "interval": "month",
        "format": "yyyy-MM-dd"
      }
    }
  }
}
```

- 销售日期按季度分桶，再查每季度销售总额、以及按制造商分桶，再制造商桶内的销售额
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "sales": {
      "date_histogram": {
        "field": "sold",
        "interval": "quarter",
        "format": "yyyy-MM-dd"
      },
      "aggs": {
        "per_make_sum": {
          "terms": {
            "field": "make"
          },
          "aggs": {
            "sum_price": {
              "sum": {
                "field": "price"
              }
            }
          }
        },
        "total_sum": {
          "sum": {
            "field": "price"
          }
        }
      }
    }
  }
}
```

- 在范围内查询（先query再aggs）
```json
GET /cars/transactions/_search
{
  "query": {
    "match": {
      "make": "ford"
    }
  },
  "aggs": {
    "colors": {
      "terms": {
        "field": "color"
      }
    }
  }
}
```

- 部分数据平均与全局数据做比对（全局桶）
```json
GET /cars/transactions/_search
{
  "query": {
    "match": {
      "make": "ford"
    }
  },
  "aggs": {
    "ford_avg_price": {
      "avg": {
        "field": "price"
      }
    },
    "all": {
      "global": {},
      "aggs": {
        "all_avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  }
}
```

- 价格大于1000的数据做平均值
```json
GET /cars/transactions/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "range": {
          "price": {
            "gte": 1000
          }
        }
      }
    }
  },
  "aggs": {
    "avg_price": {
      "avg": {
        "field": "price"
      }
    }
  }
}
```
也可使用
```json
GET /cars/transactions/_search
{
  "query": {
    "bool": {
      "filter": {
        "range": {
          "price": {
            "gte": 1000
          }
        }
      }
    } 
  },
  "aggs": {
    "avg_price": {
      "avg": {
        "field": "price"
      }
    }
  }
}
```
> 区别在于： `constant_score` 不会进行TF-IDF评分

- 过滤桶
```json
GET /cars/transactions/_search
{
    "size": 0,
    "query": {
        "match": {
            "make": "ford"
        }
    },
    "aggs": {
        "recent_sales": {
            "filter": {
                "range": {
                    "sold": {
                        "from": "now-7y"
                    }
                }
            },
            "aggs": {
                "avg_price": {
                    "avg": {
                        "field": "price"
                    }
                }
            }
        }
    }
}
```

- 后过滤器
```json
GET /cars/transactions/_search
{
    "size": 0,
    "query": {
        "match": {
            "make": "ford"
        }
    },
    "post_filter": {
        "term": {
            "color": "green"
        }
    },
    "aggs": {
        "all_colors": {
            "terms": {
                "field": "color"
            }
        }
    }
}
```

- 桶排序
```json
GET /cars/transactions/_search
{
  "aggs": {
    "order_terms": {
      "terms": {
        "field": "color",
        "order": {
          "_count": "asc"
        }
      }
    }
  }
}
```

- 按颜色分桶，再按每桶的平均价格对桶进行排序
```json
GET /cars/transactions/_search
{
  "size": 0,
  "aggs": {
    "colors": {
      "terms": {
        "field": "color",
        "order": {
          "avg_price": "desc"
        }
      },
      "aggs": {
        "avg_price": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  }
}
```

- 按价格画直方图，按方差对桶排序，过滤出红色和蓝色
```json
GET /cars/transactions/_search
{
  "aggs": {
    "colors": {
      "histogram": {
        "field": "price",
        "interval": 20000,
        "order": {
          "red_green_cars>stats.variance": "asc"
        }
      },
      "aggs": {
        "red_green_cars": {
          "filter": {
            "terms": {
              "color": [
                "red",
                "green"
              ]
            }
          },
          "aggs": {
            "stats": {
              "extended_stats": {
                "field": "price"
              }
            }
          }
        }
      }
    }
  }
}
```

## 问题
- 想要按字段值分桶时，没想到被分词了，此时在字段名后加上 `.keyword` 即可

- es默认时间格式为 `yyyy-MM-dd` ，如果想要存储 `yyyy-MM-dd HH:mm:ss` 格式，需设置_mapping中format属性（但es不支持修改_mapping）
> 假设需要修改_mapping，[只能使用数据迁移的方式](https://blog.csdn.net/lln_avaj/article/details/85048633)。例如 `/test/item` 中存在字段 `finishTime`，值为 "2020-04-21 19:05:49"
```json
# 创建新的版本的索引
PUT test_v2

# 创建映射关系，并指定日期格式
POST test_v2/item/_mapping
{
  "properties": {
    "finishTime": {
      "type": "date",
      "format": "yyyy-MM-dd HH:mm:ss"
    }
  }
}

# 迁移数据
POST _reindex
{
  "source": {
    "index": "test",
    "type": "item"
  },
  "dest": {
    "index": "test_v2",
    "type": "item"
  }
}

# 验证数据后删除index
DELETE test

# 为原本index创建索引别名
POST /_aliases
{
  "actions": [
    {
      "add": {
        "index": "test_v2",
        "alias": "test"
      }
    }
  ]
}
```


































