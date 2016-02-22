(ns messenger.android.core
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android]]
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]))

(set! js/React (js/require "react-native"))
(def react-native-contacts (js/require "react-native-contacts"))

(def app-registry (.-AppRegistry js/React))
(def logo-icon (js/require "./images/logo.png"))
(def user-no-photo (js/require "./images/user.png"))
(def seen-icon (js/require "./images/seen.png"))
(def delivered-icon (js/require "./images/delivered.png"))

(defui Contact
  static om/Ident
  (ident [this {:keys [name]}]
         [:contact/by-name name])
  static om/IQuery
  (query [this]
         '[:name :photo :delivery-status :datetime :new-messages-count :online])
  Object
  (render [this]
          (let [{:keys [name photo delivery-status datetime new-messages-count online]}
                (om/props this)]
            (view {:style {:flexDirection "row"
                           :marginTop 5
                           :marginBottom 5
                           :paddingLeft 15
                           :paddingRight 15
                           :height 75
                           :transform [{:translateX 0}
                                       {:translateY 0}]}}
                  (view {}
                        (image {:source (if (< 0 (count photo))
                                          {:uri photo}
                                          user-no-photo)
                                :style {:borderWidth 2
                                        :borderColor "#FFFFFF"
                                        ;; :borderStyle "solid"
                                        :borderRadius 50
                                        :width 54
                                        :height 54}})
                        (when online
                          (view {:style {:width 12
                                         :height 12
                                         :top 40
                                         :left 40
                                         :backgroundColor "#6BC6C8"
                                         :borderRadius 50
                                         :position "absolute"
                                         }})))
                  (view {:style {:flexDirection "column"
                                 :marginLeft 20
                                 :marginRight 10
                                 :flex 1
                                 :position "relative"}}
                        (text {:style {:fontSize 15}} name)
                        (text {:style {:color "#AAB2B2"
                                       :fontFamily "Avenir-Roman"
                                       :fontSize 14
                                       :marginTop 2
                                       :paddingRight 10}}
                              (str "Hi, I'm " name)))
                  (view {:style {:flexDirection "column"}}
                        (view {:style {:flexDirection "row"
                                       :position "absolute"
                                       :top 0
                                       :right 0}}
                              (when delivery-status
                                (image {:source (if (= (keyword delivery-status) :seen)
                                                  seen-icon
                                                  delivered-icon)
                                        :style {:marginTop 5}}))
                              (text {:style {:fontFamily "Avenir-Roman"
                                             :fontSize 11
                                             :color "#AAB2B2"
                                             :letterSpacing 1
                                             :lineHeight 15
                                             :marginLeft 5}}
                                    datetime))
                        (when (< 0 new-messages-count)
                          (view {:style {:position "absolute"
                                         :right 0
                                         :bottom 24
                                         :width 18
                                         :height 18
                                         :backgroundColor "#6BC6C8"
                                         :borderColor "#FFFFFF"
                                         :borderRadius 50
                                         :alignSelf "flex-end"}}
                                (text {:style {:width 18
                                               :height 17
                                               :fontFamily "Avenir-Roman"
                                               :fontSize 10
                                               :color "#FFFFFF"
                                               :lineHeight 19
                                               :textAlign "center"
                                               :top 1}}
                                      new-messages-count))))))))

(def contact (om/factory Contact {:keyfn :name}))

(defn render-row [row section-id row-id]
  (contact (js->clj row :keywordize-keys true)))

(defn generate-contact [n]
  {:name (str "Contact " n)
   :photo ""
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :datetime "15:30"
   :new-messages-count (rand-int 3)
   :online (< (rand) 0.5)})

(defn generate-contacts [n]
  (map generate-contact (range 1 (inc n))))

(defn load-contacts []
  (swap! state/app-state update :contacts-ds
         #(clone-with-rows %
                           (vec (generate-contacts 100))))
  
  ;; (.getAll react-native-contacts
  ;;          (fn [error raw-contacts]
  ;;            (when (not error)
  ;;              (let [contacts (map (fn [contact]
  ;;                                    (merge (generate-contact 1)
  ;;                                           {:name (:givenName contact)
  ;;                                            :photo (:thumbnailPath contact)})) 
  ;;                                  (js->clj raw-contacts :keywordize-keys true))]
  ;;                (swap! state/app-state update :contacts-ds
  ;;                       #(clone-with-rows % contacts))))))
  )

(defui AppRoot
  static om/IQuery
  (query [this]
         '[:contacts-ds])
  Object
  (componentDidMount [this]
                     (load-contacts))
  (render [this]
          (let [{:keys [contacts-ds]} (om/props this)]
            (view {:style {:flex 1}}
                  (toolbar-android {:logo logo-icon
                                    :title "Chats"
                                    :style {:backgroundColor "#e9eaed"
                                            :height 56}})
                  (list-view {:dataSource contacts-ds
                              :renderRow render-row
                              :style {}})))))

(swap! state/app-state assoc :contacts-ds
       (data-source {:rowHasChanged (fn [row1 row2]
                                      (not= row1 row2))}))

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
  (om/add-root! state/reconciler AppRoot 1)
  (.registerComponent app-registry "Messenger" (fn [] app-root)))
