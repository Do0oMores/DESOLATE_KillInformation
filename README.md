# DESOLATE KILL INFORMATION  
测试使用服务端：`CatServer1.18.2`  
前置插件：[`Vault`](https://github.com/MilkBowl/Vault) 和任意一个经济插件
* 基于 `Spigot` 开发

---  

## *击杀信息*  

<details><summary><b>击杀提示</b></summary>

<img src="src/main/resources/img/img.png" alt="kill tips example">

- 可自定义全服提示或仅在当前世界提示：`只给相同世界的玩家发送信息`
> 鼠标悬浮在物品上可显示物品详细信息
</details>

<details><summary><b>击杀记录</b></summary>

- 记录击杀信息到 `data.yml`
- 可对接 [`Kookbot`](https://github.com/DESOLATE-CRG/DESOLATE-KookBot-MC) 在KOOK频道内查询击杀信息
- 命令：`/kf cx` 可查询玩家十条击杀信息
> 物品需要有Display Name才可记录击杀信息
</details>

<details><summary><b>物品击杀追踪</b></summary>
<p align="center">
<img src="src/main/resources/img/img_1.png" alt="item kill track example" height="300">
</p>

- 记录物品的击杀数并显示
- 将物品放到副手使用命令 `/kf killtrack` 花费经济对该物品启用击杀追踪
> 更改 `config.yml` 的 `击杀记录` 可自定义解锁该功能需要花费的经济
</details>

<details><summary><b>连杀提示</b></summary>

- 在三十秒内造成连杀将提示该世界所有玩家
</details>

---

* 还有其他想法或问题？
* 欢迎加入我的QQ群联系我：`772493975`