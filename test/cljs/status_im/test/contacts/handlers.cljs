(ns status-im.test.contacts.handlers
  (:require [cljs.test :refer-macros [deftest is]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            status-im.specs
            status-im.db
            [status-im.contacts.events :as e]
            [status-im.handlers :as h]
            status-im.subs))

(def browse-contact-from-realm-db
  {:last-updated     0
   :address          nil
   :name             "Browse"

   :global-command
                     {
                      :description         "Launch the browser"
                      :sequential-params   false
                      :color               "#ffa500"
                      :name                "global"
                      :params
                                           {
                                            :0
                                            {
                                             :name        "url"
                                             :type        "text"
                                             :placeholder "URL"}}
                      :icon                nil
                      :title               "Browser"
                      :has-handler         false
                      :fullscreen          true
                      :suggestions-trigger "on-change"}
   :dapp-url         nil
   :dapp-hash        nil

   :commands
                     {
                      :location
                      {
                       :description         "Share your location"
                       :sequential-params   true
                       :color               nil
                       :name                "location"

                       :params
                                            {
                                             :0
                                             {
                                              :name        "address"
                                              :type        "text"
                                              :placeholder "address"}}
                       :icon                nil
                       :title               "Location"
                       :has-handler         false
                       :fullscreen          true
                       :owner-id            "browse"
                       :suggestions-trigger "on-change"}}
   :photo-path       nil
   :debug?           false
   :status           nil
   :bot-url          "local://browse-bot"
   :responses        {}
   :pending?         false
   :whisper-identity "browse"
   :last-online      0
   :dapp?            true
   :unremovable?     true
   :private-key      nil
   :public-key       nil})

(def browse-global-commands
  {:browse
   {
    :description         "Launch the browser"
    :bot                 "browse"
    :color               "#ffa500"
    :name                "global"
    :params
                         [
                          {
                           :name        "url"
                           :placeholder "URL"
                           :type        "text"}]
    :type                :command
    :title               "Browser"
    :sequential-params   false
    :icon                nil
    :has-handler         false
    :fullscreen          true
    :suggestions-trigger "on-change"}})

#_{:registered-only true
   :has-handler     false
   :fullscreen      true
   :hidden?         nil}

(def browse-contatcs
  {
   "browse"
                     {
                      :last-updated     0
                      :address          nil
                      :name             "Browse"

                      :global-command
                                        {
                                         :description         "Launch the browser"
                                         :sequential-params   false
                                         :color               "#ffa500"
                                         :name                "global"

                                         :params
                                                              {

                                                               :0
                                                               {
                                                                :name        "url"
                                                                :type        "text"
                                                                :placeholder "URL"}}
                                         :icon                nil
                                         :title               "Browser"
                                         :has-handler         false
                                         :fullscreen          true
                                         :suggestions-trigger "on-change"}
                      :commands-loaded? true
                      :dapp-url         nil
                      :dapp-hash        nil
                      :subscriptions    {}

                      :commands
                                        {

                                         :location
                                         {
                                          :description       "Share your location"
                                          :bot               "mailman"
                                          :sequential-params true
                                          :hide-send-button  true
                                          :name              "location"

                                          :params
                                                             [

                                                              {
                                                               :name        "address"
                                                               :placeholder "address"
                                                               :type        "text"}]
                                          :type              :command
                                          :title             "Location"
                                          :has-handler       false
                                          :hidden?           nil
                                          :owner-id          "mailman"}}}
   :photo-path       nil
   :debug?           false
   :status           nil
   :bot-url          "local://browse-bot"
   :responses        {}
   :pending?         false
   :whisper-identity "browse"
   :last-online      0
   :dapp?            true
   :unremovable?     true
   :private-key      nil
   :public-key       nil})

(defn test-fixtures
  []
  (rf/reg-cofx
    ::e/get-all-contacts
    (fn [coeffects _]
      (assoc coeffects :all-contacts [browse-contact-from-realm-db])))
  (rf/reg-fx
    ::h/init-store
    (fn []
      nil)))

(deftest basic-sync
  (run-test-sync
    (test-fixtures)
    (rf/dispatch [:initialize-db])
    (let [contacts (rf/subscribe [:get-contacts])
          global-commands (rf/subscribe [:get :global-commands])]

      ;;Assert the initial state
      (is (and (map? @contacts) (empty? @contacts)))
      (is (nil? @global-commands))

      (rf/dispatch [:load-contacts])

      (is (= {"browse" browse-contact-from-realm-db} @contacts))
      (is (= browse-global-commands @global-commands)))))
