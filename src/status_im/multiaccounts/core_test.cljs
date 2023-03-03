(ns status-im.multiaccounts.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.multiaccounts.core :as ma]))

(def compressed-key "zQsomething")
(def short-compressed-key "zQsome…ing")
(def public-key
  "0x0461f576da67dc0bca9888cdb4cb28c80285b756b324109da94a081585ed6f007cf00afede6b3ee5638593674fee100b590318fc7bdb0054b8dd9445acea216ad2")
(def short-public-key "0x0461…ad2")
(def random-name "Studious Gold Mustang")
(def override-random-name (str "override" random-name))
(def nickname "nickname")
(def override-nickname (str nickname "override"))
(def display-name "display-name")
(def override-display-name (str display-name "override"))
(def ens-name "jakubgs.eth")
(def formatted-ens (str "@" ens-name))
(def override-ens-name (str "override" ens-name))

(def contact
  {:nickname       nickname
   :name           ens-name
   :ens-verified   true
   :compressed-key compressed-key
   :public-key     public-key
   :display-name   display-name})

(deftest contact-two-names-test
  (testing "names is nil"
    (testing "nickname has precedence"
      (is
       (= [nickname ens-name]
          (ma/contact-two-names contact public-key))))
    (testing "ens name is second option"
      (is
       (= [ens-name display-name]
          (ma/contact-two-names
           (dissoc contact :nickname)
           public-key))))
    (testing "ens name is second option but not verified"
      (is
       (= [display-name random-name]
          (ma/contact-two-names
           (dissoc contact :nickname :ens-verified)
           public-key))))
    (testing "display name is third option"
      (is
       (= [display-name random-name]
          (ma/contact-two-names
           (dissoc contact :nickname :name)
           public-key))))
    (testing "3 random words is fallback"
      (is
       (= [random-name short-compressed-key]
          (ma/contact-two-names
           (dissoc contact
            :nickname
            :name
            :display-name)
           public-key)))))
  (testing "public-key is the least favorite"
    (is
     (= [random-name short-public-key]
        (ma/contact-two-names
         (dissoc contact
          :nickname
          :name
          :compressed-key
          :display-name)
         public-key))))
  (testing "names is provided"
    (let [names              {:nickname         override-nickname
                              :display-name     override-display-name
                              :three-words-name override-random-name
                              :ens-name         override-ens-name}
          contact-with-names (assoc contact :names names)]

      (testing "nickname has precedence"
        (is
         (= [override-nickname override-ens-name]
            (ma/contact-two-names contact-with-names public-key))))
      (testing "ens name is second option"
        (is
         (= [override-ens-name override-display-name]
            (ma/contact-two-names
             (update contact-with-names :names dissoc :nickname)
             public-key))))
      (testing "display name is third option"
        (is
         (= [override-display-name override-random-name]
            (ma/contact-two-names
             (update contact-with-names :names dissoc :nickname :ens-name)
             public-key))))
      (testing "3 random words is fallback"
        (is
         (= [override-random-name short-compressed-key]
            (ma/contact-two-names
             (update contact-with-names :names dissoc :nickname :ens-name :display-name)
             public-key)))))))
