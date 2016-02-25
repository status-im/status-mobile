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
         '[:id :body :outgoing :delivery-status :datetime])
  Object
  (render [this]
          (let [{:keys [id body outgoing delivery-status datetime]}
                (om/props this)]
            (view {:height 70}
                  (text {:style {:marginVertical 10
                                 :fontFamily "Avenir-Roman"
                                 :fontSize 11
                                 :color "#AAB2B2"
                                 :letterSpacing 1
                                 :lineHeight 15
                                 :textAlign "center"
                                 :opacity 0.8}}
                        datetime)
                  (view {:style (merge {:borderRadius 6
                                        :marginVertical 5
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
                              body))))))

(def message (om/factory Message {:keyfn :id}))

(defn render-row [row section-id row-id]
  (message (js->clj row :keywordize-keys true)))

(defn generate-message [n]
  {:id n
   :body "This is a text. This is a text."
   :outgoing (< (rand) 0.5)
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :datetime "15:30"})

(defn generate-messages [n]
  (map generate-message (range 1 (inc n))))

(defn load-messages []
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= row1 row2))})
                   (vec (generate-messages 10))))

(defui Chat
  ;; static om/IQuery
  ;; (query [this]
  ;;        '[:contacts-ds])
  Object
  ;; (componentDidMount [this]
  ;;                    (load-contacts))
  (render [this]
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
                              :renderRow render-row
                              :style {}})))))

(def chat (om/factory Chat))
