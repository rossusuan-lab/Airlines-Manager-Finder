cmake_minimum_required(VERSION 3.10.2)

add_library(pairipcore SHARED pairipcore.cpp)

find_library(log-lib log)

target_link_libraries(pairipcore ${log-lib})


add_library(crypto-lib SHARED crypto-lib.cpp)

find_library(log-lib log)

target_link_libraries(crypto-lib ${log-lib})


add_library(main-lib SHARED main-lib.cpp)

find_library(log-lib log)

target_link_libraries(main-lib ${log-lib})


add_library(math-lib SHARED math-lib.cpp)

find_library(log-lib log)

target_link_libraries(math-lib ${log-lib})


add_library(native-lib SHARED native-lib.cpp)

find_library(log-lib log)

target_link_libraries(native-lib ${log-lib})


add_library(utils-lib SHARED utils-lib.cpp)

find_library(log-lib log)

target_link_libraries(utils-lib ${log-lib})