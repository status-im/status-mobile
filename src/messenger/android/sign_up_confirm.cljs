(ns messenger.android.sign-up-confirm
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android text-input]]
   [natal-shell.async-storage :refer [get-item set-item]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [syng-im.protocol.web3 :as whisper]
            [messenger.state :as state]
            [messenger.android.utils :refer [log toast http-post]]
            [messenger.android.crypt :refer [encrypt]]
            [messenger.android.resources :as res]
            [messenger.android.database :as db]
            [messenger.android.contacts :as contacts]
            [messenger.android.contacts-list :refer [contacts-list]]))

(def nav-atom (atom nil))

(defn show-home-view []
  (binding [state/*nav-render* false]
    (.replace @nav-atom (clj->js {:component contacts-list
                                  :name "contacts-list"}))))

(defn handle-load-contacts-identities-response [contacts-by-hash data]
  (let [contacts (map (fn [contact]
                        {:phone-number (get contacts-by-hash
                                            (:phone-number-hash contact))
                         :whisper-identity (:whisper-identity contact)})
                      (js->clj (:contacts data)))]
    (db/add-contacts contacts)
    (show-home-view)))

(defn get-contacts-by-hash [contacts]
  (let [numbers (reduce (fn [numbers contact]
                          (into numbers
                                (map :number (:phone-numbers contact))))
                        '()
                        contacts)]
    (reduce (fn [m number]
              (let [hash (encrypt number)]
                (assoc m hash number)))
            {}
            numbers)))

(defn send-load-contacts-identities [contacts]
  (let [contacts-by-hash (get-contacts-by-hash contacts)
        data (keys contacts-by-hash)]
    (http-post "get-contacts" {:phone-number-hashes data}
               (partial handle-load-contacts-identities-response contacts-by-hash)
               (fn [error]
                 (toast (str error))))))

(defn load-contacts []
  (contacts/load-contacts
   send-load-contacts-identities
   (fn [error]
     (toast (str error)))))

(defn handle-send-code-response [body]
  (log body)
  (toast (if (:confirmed body)
           "Confirmed"
           "Wrong code"))
  (when (:confirmed body)
    (load-contacts)))

(defn code-valid? [code]
  (= 4 (count code)))

(defn send-code [code]
  (when (code-valid? code)
    (http-post "sign-up-confirm"
               {:code code}
               handle-send-code-response)))

(defn update-code [value]
  (let [formatted value]
    (swap! state/app-state assoc :confirmation-code formatted)))

(defui SignUpConfirm
  static om/IQuery
  (query [this]
         '[:confirmation-code])
  Object
  (render
   [this]
   (let [{:keys [confirmation-code]} (om/props this)
         {:keys [nav]} (om/get-computed this)]
     (reset! nav-atom nav)
     (view
      {:style {:flex 1
               :backgroundColor "white"}}
      (toolbar-android {:logo res/logo-icon
                        :title "Confirm"
                        :titleColor "#4A5258"
                        :style {:backgroundColor "white"
                                :height 56
                                :elevation 2}})
      (view {}
            (text-input {:underlineColorAndroid "#9CBFC0"
                         :placeholder "Enter confirmation code"
                         :keyboardType "number-pad"
                         :maxLength 4
                         :onChangeText (fn [value]
                                         (update-code value))
                         :style {:flex 1
                                 :marginHorizontal 18
                                 :lineHeight 42
                                 :fontSize 14
                                 :fontFamily "Avenir-Roman"
                                 :color "#9CBFC0"}}
                        confirmation-code)
            (if (code-valid? confirmation-code)
              (touchable-highlight
               {:onPress #(send-code confirmation-code)
                :style {:alignSelf "center"
                        :borderRadius 7
                        :backgroundColor "#E5F5F6"
                        
                        :width 100}}
               (text {:style {:marginVertical 10
                              :textAlign "center"}}
                     "Confirm"))
              (view
               {:style {:alignSelf "center"
                        :borderRadius 7
                        :backgroundColor "#AAB2B2"
                        :width 100}}
               (text {:style {:marginVertical 10
                              :textAlign "center"}}
                     "Confirm"))))))))

(def sign-up-confirm (om/factory SignUpConfirm))
