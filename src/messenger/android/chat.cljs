(ns messenger.android.chat
  (:require-macros
   [natal-shell.components :refer [view text image touchable-highlight list-view
                                   toolbar-android]]
   [natal-shell.data-source :refer [data-source clone-with-rows]]
   [natal-shell.core :refer [with-error-view]]
   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [messenger.state :as state]
            [messenger.android.resources :as res]))

(defn nav-pop [nav]
  (binding [state/*nav-render* false]
    (.pop nav)))

(defui Message
  static om/Ident
  (ident [this {:keys [id]}]
         [:message/by-id id])
  static om/IQuery
  (query [this]
         '[:id :body :delivery-status :datetime])
  Object
  (render [this]
          (let [{:keys [id body delivery-status datetime]}
                (om/props this)]
            (text {:style {:color "#AAB2B2"
                           :fontFamily "Avenir-Roman"
                           :fontSize 14
                           :marginTop 2
                           :paddingRight 10}}
                  body))))

(def message (om/factory Message {:keyfn :id}))

(defn render-row [row section-id row-id]
  (message (js->clj row :keywordize-keys true)))

(defn generate-message [n]
  {:id n
   :body "Hi"
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :datetime "15:30"})

(defn generate-messages [n]
  (map generate-message (range 1 (inc n))))

(defn load-message []
  (swap! state/app-state update :contacts-ds
         #(clone-with-rows %
                           (vec (generate-messages 10)))))

(defui Chat
  ;; static om/IQuery
  ;; (query [this]
  ;;        '[:contacts-ds])
  Object
  ;; (componentDidMount [this]
  ;;                    (load-contacts))
  (render [this]
          (let [{:keys [contacts-ds]} (om/props this)
                {:keys [nav]} (om/get-computed this)]
            (view {:style {:flex 1}}
                  (toolbar-android {:logo res/logo-icon
                                    :title "Chat name"
                                    :style {:backgroundColor "#e9eaed"
                                            :height 56}
                                    :navIcon res/nav-back-icon
                                    :onIconClicked (fn []
                                                     (nav-pop nav))})
                  (text {} "Hello")
                  ;; (list-view {:dataSource contacts-ds
                  ;;             :renderRow render-row
                  ;;             :style {}})
                  ))))

(def chat (om/factory Chat))
