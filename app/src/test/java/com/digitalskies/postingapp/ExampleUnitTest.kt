package com.digitalskies.postingapp

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun rr(){
        var gg="uoo"
        var tt=HashMap<Char,Int>()

        tt['c']=1

        tt['c']=2

        var yy=gg.substring(1)








        println("username is ${gg.indexOf(yy)}")
    }

    fun MinWindowSubstring(strArr: Array<String>): String {

        // code goes here


        var subStringK=strArr[1]

        var mainStringC=strArr[0]


        var beginningIndex=mainStringC.indexOf(subStringK)

        if(beginningIndex!=-1){

            return subStringK
        }


        var indexArray=ArrayList<Int>(subStringK.length)

        var characterHashMap=HashMap<Char,Int>()

        for(c in subStringK){

            var lastIndex:Int=mainStringC.indexOf(c)


            var temp=lastIndex

            while(mainStringC.substring(temp+1).contains(c)){

                lastIndex=mainStringC.substring(temp+1).indexOf(c)

                temp+=1

            }

            if(characterHashMap.contains(c)){

                characterHashMap[c]?.let {
                    lastIndex =mainStringC.substring(it+1).indexOf(c)
                }
            }



            characterHashMap[c]=lastIndex

            indexArray.add(lastIndex)

        }

        indexArray.sort()


        return mainStringC.substring(indexArray.first(),indexArray.last().plus(1));

    }


}