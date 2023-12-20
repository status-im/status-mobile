(ns utils.universal-links-test
  (:require
    [cljs.test :refer-macros [deftest testing are]]
    matcher-combinators.test
    [utils.universal-links :as links]))

(deftest universal-link-test
  (testing "universal-link?"
    (are [l rst] (match? (links/universal-link? l) rst)
     "status-app://blah"
      false
     "http://status.app/blah"
      false
     "http://status.app/c#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      true
     "http://status.app/u#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      true
     "https://status.app/u/Ow==#zQ3shsKnV5HJMWJR61c6dssWzHszdbLfBoMF1gcLtSQAYdw2d"
      true
     "https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc11"
      true
     "https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc111"
      false
     "https://status.app/u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSj"
      true
     "https://status.app/c/G00AAGS9TbI9mSR-ZNmFrhRjNuEeXAAbcAIUaLLJyjMOG3ACJQ12oIHD78QhzO9s_T5bUeU7rnATWJg3mGgTUemrAg==#zQ3shYf5SquxkiY3FmCW6Nz2wuFWFcM6JEdUD62ApjAvE5YPv"
      true
     "http://status.app/c#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgBhttp://status.app/c#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      false
     "http://status.app/u#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgBhttp://status.app/u#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      false
     "https://status.app/u/Ow==#zQ3shsKnV5HJMWJR61c6dssWzHszdbLfBoMF1gcLtSQAYdw2dhttps://status.app/u/Ow==#zQ3shsKnV5HJMWJR61c6dssWzHszdbLfBoMF1gcLtSQAYdw2d"
      false
     "https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc11https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc11"
      false
     "https://status.app/u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSjhttps://status.app/u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSj"
      false
     "https://status.app/c/Ow==#zQ3shbmfT3hvh4mKa1v6uAjjyztQEroh8Mfn6Ckegjd7LT3XKhttps://status.app/c#zQ3shbmfT3hvh4mKa1v6uAjjyztQEroh8Mfn6Ckegjd7LT3XK"
      false
     "https://status.app/blah"
      false
     "https://status.app/browse/www.аррӏе.com"
      false
     "https://not.status.im/blah"
      false
     "http://not.status.im/blah"
      false))

  (testing "deep-link?"
    (are [l rst] (match? (links/deep-link? l) rst)
     "status-app://blah"                                   true
     "http://status.app/blah"                              false
     "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" true)))
