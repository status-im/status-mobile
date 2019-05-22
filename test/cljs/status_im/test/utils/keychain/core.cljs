(ns status-im.test.utils.keychain.core
  (:require [cljs.test :refer-macros [deftest async is testing]]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.keychain.core :as keychain]))

(def strong-key (range 0 64))
(def weak-key (concat (range 0 32) (take 32 (repeat 0))))

(defn- key->json [k]
  (->> k
       (keychain/bytes->js-array)
       (.stringify js/JSON)))

(deftest key-does-not-exists
  (async
   done
   (with-redefs [rn/keychain (fn [] #js {:getGenericPassword (constantly (.resolve js/Promise nil))})]
     (testing "it returns a valid key"
       (.. (keychain/get-encryption-key)
           (then (fn [k]
                   (is (= strong-key (js->clj k)))
                   (done)))
           (catch (fn [err]
                    (is (not err))
                    (done))))))))

(deftest key-does-exists
  (async
   done
   (with-redefs [rn/keychain (fn [] #js {:getGenericPassword (constantly (.resolve js/Promise #js {:password (key->json (range 64 128))}))})]
     (testing "it returns a valid key"
       (.. (keychain/get-encryption-key)
           (then (fn [k]
                   (is (= (range 64 128) (js->clj k)))
                   (done)))
           (catch (fn [err]
                    (is (not err))
                    (done))))))))

(deftest key-is-weak
  (async
   done
   (with-redefs [rn/keychain (fn [] #js {:getGenericPassword (constantly (.resolve js/Promise #js {:password (key->json weak-key)}))})
                 keychain/generic-password (atom nil)]
     (testing "it returns a valid key"
       (.. (keychain/get-encryption-key)
           (then (fn [_]
                   (is false)
                   (done)))
           (catch (fn [{:keys [error key]}]
                    (is (= :weak-key error))
                    (is (= weak-key (js->clj key)))
                    (done))))))))

(deftest safe-key-is-not-valid
  (async
   done
   (with-redefs [rn/keychain (fn [] #js {:getGenericPassword (constantly (.resolve js/Promise #js {:password (key->json weak-key)}))})
                 keychain/generic-password (atom nil)]
     (testing "it returns a valid key"
       (.. (keychain/safe-get-encryption-key)
           (then (fn [k]
                   (is (= weak-key (js->clj k)))
                   (done)))
           (catch (fn [err]
                    (is (not err))
                    (done))))))))

(deftest safe-key-is-nil
  (async
   done
   (with-redefs [rn/keychain (fn [] #js {:getGenericPassword (constantly (.resolve js/Promise #js {:password nil}))})
                 keychain/generic-password (atom nil)]
     (testing "it returns a valid key"
       (.. (keychain/safe-get-encryption-key)
           (then (fn [k]
                   (is (= "" (js->clj k)))
                   (done)))
           (catch (fn [err]
                    (is (not err))
                    (done))))))))
