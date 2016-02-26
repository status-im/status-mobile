(ns messenger.android.chat
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   text-input toolbar-android]]
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]
            [messenger.android.resources :as res]))

(set! js/InvertibleScrollView (js/require "react-native-invertible-scroll-view"))

(defn nav-pop [nav]
  (binding [state/*nav-render* false]
    (.pop nav)))

(defui Message
  static om/Ident
  (ident [this {:keys [id]}]
         [:message/by-id id])
  static om/IQuery
  (query [this]
         '[:id :body :outgoing :delivery-status :date :new-day])
  Object
  (render
   [this]
   (let [{:keys [id body outgoing delivery-status date new-day]}
         (om/props this)]
     (view {:paddingHorizontal 15}
           ;;; date
           (when new-day
             (text {:style {:marginVertical 10
                            :fontFamily "Avenir-Roman"
                            :fontSize 11
                            :color "#AAB2B2"
                            :letterSpacing 1
                            :lineHeight 15
                            :textAlign "center"
                            :opacity 0.8}}
                   date))
           ;;; body
           (view {:style (merge {:width 260
                                 :marginVertical 5}
                                (if outgoing
                                  {:alignSelf "flex-end"}
                                  {:alignSelf "flex-start"}))}
                 (view {:style (merge {:borderRadius 6
                                       :paddingVertical 12
                                       :paddingHorizontal 16}
                                      (if outgoing
                                        {:backgroundColor "#D3EEEF"
                                         :alignSelf "flex-end"}
                                        {:backgroundColor "#FBF6E3"
                                         :alignSelf "flex-start"}))}
                       (text {:style {:fontSize 14
                                      :fontFamily "Avenir-Roman"
                                      :color "#4A5258"}}
                             body)))))))

(def message (om/factory Message {:keyfn :id}))

(defn render-row [row section-id row-id]
  (message (js->clj row :keywordize-keys true)))

(defn generate-message [n]
  {:id n
   :body (if (= (rem n 3) 0)
           (apply str n "." (repeat 5 " This is a text."))
           (str n ". This is a text."))
   :outgoing (< (rand) 0.5)
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :date "TODAY"
   :new-day (= (rem n 3) 0)})

(defn generate-messages [n]
  (map generate-message (range 1 (inc n))))

(defn load-messages []
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= row1 row2))})
                   (vec (generate-messages 100))))

(defui NewMessage
  Object
  (render
   [this]
   (view {}
         (text-input {}))))

(def new-message (om/factory NewMessage))

(defui Chat
  Object
  (render
   [this]
   (let [{:keys [nav]} (om/get-computed this)
         messages-ds (load-messages)]
     (view {:style {:flex 1}}
           (toolbar-android {:logo res/logo-icon
                             :title "Chat name"
                             :titleColor "#4A5258"
                             :subtitle "Last seen just now"
                             :subtitleColor "#AAB2B2"
                             :navIcon res/nav-back-icon
                             :style {:backgroundColor "#e9eaed"
                                     :height 56}
                             :onIconClicked (fn []
                                              (nav-pop nav))})
           (list-view {:dataSource messages-ds
                       :renderScrollComponent
                       (fn [props]
                         (js/React.createElement js/InvertibleScrollView
                                                 (clj->js (merge (js->clj props)
                                                                 {:inverted true}))))
                       :renderRow render-row
                       :style {}})
           (new-message)))))

(def chat (om/factory Chat))
