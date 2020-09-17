# Useful commands

## Show logcat on many devices
```
adb -s F5AZB702J659 logcat dupa0:D dupa1:D dupa2:D dupa3:D dupa4:D *:S -v raw -v color
```
## Show name
```
adb -s 43d075de shell getprop ro.product.model
```
