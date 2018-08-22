(ns status-im.test.browser.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            [status-im.ui.screens.events :as events]
            status-im.ui.screens.db
            status-im.ui.screens.subs
            [re-frame.core :as re-frame]
            [status-im.models.browser :as model]
            [status-im.utils.types :as types]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.models.browser :as browser]))

(defn test-fixtures []

  (re-frame/reg-fx ::events/init-store #())

  (re-frame/reg-fx :browse #())
  (re-frame/reg-fx :data-store/tx #())

  (re-frame/reg-cofx
   :data-store/all-browsers
   (fn [coeffects _]
     (assoc coeffects :all-stored-browsers [])))

  (re-frame/reg-cofx
   :data-store/all-dapp-permissions
   (fn [coeffects _]
     (assoc coeffects :all-dapp-permissions [])))

  (re-frame/reg-fx :send-to-bridge-fx #())

  (re-frame/reg-fx
   :show-dapp-permission-confirmation-fx
   (fn [[permission {:keys [dapp-name permissions-data index] :as params}]]
     (if (and (= dapp-name "test.com") (#{0 1} index))
       (re-frame/dispatch [:next-dapp-permission params permission permissions-data])
       (re-frame/dispatch [:next-dapp-permission params]))))

  (handlers/register-handler-fx
   [(re-frame/inject-cofx :data-store/all-browsers)
    (re-frame/inject-cofx :data-store/all-dapp-permissions)]
   :initialize-test
   (fn [cofx [_]]
     (handlers-macro/merge-fx cofx
                              (events/initialize-db)
                              (browser/initialize-browsers)
                              (browser/initialize-dapp-permissions)))))

(deftest browser-events

  (run-test-sync

   (test-fixtures)

   (re-frame/dispatch [:initialize-test])
   (println :app-db @re-frame.db/app-db)
   (let [browsers  (re-frame/subscribe [:browsers])
         dapp1-url "cryptokitties.co"
         dapp2-url "http://test2.com"]

     (testing "open and remove dapps"
       (println :browsers @browsers)
       (is (do (println :browser @browsers)
               (zero? (count @browsers))))

       (re-frame/dispatch [:open-url-in-browser dapp1-url])

       (is (= 1 (count @browsers)))

       (re-frame/dispatch [:open-url-in-browser dapp2-url])

       (is (= 2 (count @browsers)))

       (let [browser1 (first (vals @browsers))
             browser2 (second (vals @browsers))]
         (is (and (:dapp? browser1)
                  (not (:dapp? browser2))))
         (is (and (zero? (:history-index browser1))
                  (zero? (:history-index browser2))))
         (is (and (= [(str "http://" dapp1-url) (:history browser1)])
                  (= [dapp2-url] (:history browser2)))))

       (re-frame/dispatch [:remove-browser dapp1-url])

       (is (= 1 (count @browsers))))

     (testing "navigate dapp"

       (re-frame/dispatch [:open-browser (first (vals @browsers))])

       (let [browser    (re-frame/subscribe [:get-current-browser])
             dapp2-url2 (str dapp2-url "/nav2")
             dapp2-url3 (str dapp2-url "/nav3")]

         (is (zero? (:history-index @browser)))
         (is (= [dapp2-url] (:history @browser)))

         (is (and (not (model/can-go-back? @browser))
                  (not (model/can-go-forward? @browser))))

         (re-frame/dispatch [:browser-nav-back])
         (re-frame/dispatch [:browser-nav-forward])

         (re-frame/dispatch [:update-browser-on-nav-change @browser dapp2-url2 false])

         (is (= 1 (:history-index @browser)))
         (is (= [dapp2-url dapp2-url2] (:history @browser)))

         (is (and (model/can-go-back? @browser)
                  (not (model/can-go-forward? @browser))))

         (re-frame/dispatch [:browser-nav-back @browser])

         (is (zero? (:history-index @browser)))
         (is (= [dapp2-url dapp2-url2] (:history @browser)))

         (is (and (not (model/can-go-back? @browser))
                  (model/can-go-forward? @browser)))

         (re-frame/dispatch [:update-browser-on-nav-change @browser dapp2-url3 false])

         (is (= 1 (:history-index @browser)))
         (is (= [dapp2-url dapp2-url3] (:history @browser)))

         (re-frame/dispatch [:browser-nav-back @browser])

         (is (zero? (:history-index @browser)))
         (is (= [dapp2-url dapp2-url3] (:history @browser)))

         (re-frame/dispatch [:browser-nav-forward @browser])

         (is (= 1 (:history-index @browser)))
         (is (= [dapp2-url dapp2-url3] (:history @browser))))))

   (let [dapps-permissions (re-frame/subscribe [:get :dapps/permissions])
         dapp-name         "test.com"
         dapp-name2        "test2.org"]

     (testing "dapps permissions"

       (is (zero? (count @dapps-permissions)))

       (re-frame/dispatch [:on-bridge-message (types/clj->json {:type        "status-api-request"
                                                                :host        dapp-name
                                                                :permissions ["FAKE_PERMISSION"]})
                           nil nil])

       (re-frame/dispatch [:next-dapp-permission
                           {:dapp-name             dapp-name
                            :index                 0
                            :requested-permissions ["FAKE_PERMISSION"]
                            :permissions-data "Data"}])

       (is (= {:dapp        dapp-name
               :permissions []}
              (get @dapps-permissions dapp-name)))

       (re-frame/dispatch [:on-bridge-message (types/clj->json {:type        "status-api-request"
                                                                :host        dapp-name
                                                                :permissions ["CONTACT_CODE"]})
                           nil nil])

       (re-frame/dispatch [:next-dapp-permission
                           {:dapp-name             dapp-name
                            :index                 0
                            :requested-permissions ["CONTACT_CODE"]
                            :permissions-data {"CONTACT_CODE" "Data"}}
                           "CONTACT_CODE"
                           {"CONTACT_CODE" "Data"}])

       (is (= 1 (count @dapps-permissions)))

       (is (= {:dapp        dapp-name
               :permissions ["CONTACT_CODE"]}
              (get @dapps-permissions dapp-name)))

       (re-frame/dispatch [:on-bridge-message (types/clj->json {:type        "status-api-request"
                                                                :host        dapp-name
                                                                :permissions ["CONTACT_CODE" "FAKE_PERMISSION"]})
                           nil nil])

       (is (= 1 (count @dapps-permissions)))

       (is (= {:dapp        dapp-name
               :permissions ["CONTACT_CODE"]}
              (get @dapps-permissions dapp-name)))

       (re-frame/dispatch [:on-bridge-message (types/clj->json {:type        "status-api-request"
                                                                :host        dapp-name
                                                                :permissions ["FAKE_PERMISSION"]})
                           nil nil])

       (is (= 1 (count @dapps-permissions)))

       (is (= {:dapp        dapp-name
               :permissions ["CONTACT_CODE"]}
              (get @dapps-permissions dapp-name)))

       (re-frame/dispatch [:on-bridge-message (types/clj->json {:type        "status-api-request"
                                                                :host        dapp-name2
                                                                :permissions ["CONTACT_CODE"]})
                           nil nil])

       (re-frame/dispatch [:next-dapp-permission
                           {:dapp-name             dapp-name2
                            :index                 0
                            :requested-permissions ["CONTACT_CODE" "FAKE_PERMISSION"]
                            :permissions-data "Data"}])

       (is (= 2 (count @dapps-permissions)))

       (is (= {:dapp        dapp-name2
               :permissions []}
              (get @dapps-permissions dapp-name2)))))))
