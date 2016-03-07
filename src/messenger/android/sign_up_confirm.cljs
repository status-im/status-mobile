(ns messenger.android.sign-up-confirm
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android text-input]]
   [natal-shell.async-storage :refer [get-item set-item]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]]
   [natal-shell.toast-android :as toast])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [syng-im.protocol.whisper :as whisper]
            [messenger.state :as state]
            [messenger.android.utils :refer [log toast]]
            [messenger.android.resources :as res]
            [messenger.android.contacts-list :refer [contacts-list]]))

(def nav-atom (atom nil))

(defn show-home-view []
  (binding [state/*nav-render* false]
    (.replace @nav-atom (clj->js {:component contacts-list
                                  :name "contacts-list"}))))

(defn handle-send-check-contacts-response []
  (show-home-view))

(defn send-check-contacts []
  )

(defn handle-send-code-response [response]
  )

(defn code-valid? [code]
  (= 4 (count code)))

(defn send-code [code]
  (when (code-valid? code)
    (toast (str code))
    (show-home-view)))

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
