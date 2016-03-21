(ns messenger.components.chat.chat
  (:require-macros
    [natal-shell.components :refer [view list-view toolbar-android]]
    [natal-shell.data-source :refer [data-source clone-with-rows]])
  (:require [om.next :as om :refer-macros [defui]]
            [messenger.utils.resources :as res]
            [messenger.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [messenger.components.chat.message :refer [message]]
            [messenger.components.chat.new-message :refer [new-message]]
            [messenger.state :as state]
            [syng-im.utils.logging :as log]
            [messenger.components.iname :as in]))

(defn generate-message [n]
  {:id              n
   :type            (if (= (rem n 4) 3)
                      :audio
                      :text)
   :body            (if (= (rem n 3) 0)
                      (apply str n "." (repeat 5 " This is a text."))
                      (str n ". This is a text."))
   :outgoing        (< (rand) 0.5)
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :date            "TODAY"
   :new-day         (= (rem n 3) 0)})

(defn generate-messages [n]
  (map generate-message (range 1 (inc n))))

(defn load-messages []
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= row1 row2))})
                   (vec (generate-messages 100))))

(defn nav-pop [nav]
  (binding [state/*nav-render* false]
    (.pop nav)))

(defn render-row [row section-id row-id]
  (message (js->clj row :keywordize-keys true)))

(defui Chat
  static in/IName
  (get-name [this]
    :chat)
  static om/IQuery
  (query [this]
    '[:chat/messages])
  Object
  (render
    [this]
    (let [{:keys [nav]} (om/get-computed this)
          {{messages :chat/messages} :chat} (om/props this)
          _           (log/debug "messages=" messages)
          messages-ds (load-messages)]
      (view {:style {:flex            1
                     :backgroundColor "white"}}
            (toolbar-android {:logo          res/logo-icon
                              :title         "Chat name"
                              :titleColor    "#4A5258"
                              :subtitle      "Last seen just now"
                              :subtitleColor "#AAB2B2"
                              :navIcon       res/nav-back-icon
                              :style         {:backgroundColor "white"
                                              :height          56
                                              :elevation       2}
                              :onIconClicked (fn []
                                               (nav-pop nav))})
            (list-view {:dataSource messages-ds
                        :renderScrollComponent
                                    (fn [props]
                                      (invertible-scroll-view nil))
                        :renderRow  render-row
                        :style      {:backgroundColor "white"}})
            (new-message)))))

(def chat (om/factory Chat))

