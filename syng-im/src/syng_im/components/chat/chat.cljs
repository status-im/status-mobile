(ns syng-im.components.chat.chat
  (:require-macros
   [natal-shell.data-source :refer [data-source clone-with-rows]])
  (:require [reagent.core :as r]
            [syng-im.components.nav :as nav]
            [syng-im.components.react :refer [view list-view toolbar-android list-item]]
            [syng-im.components.resources :as res]
            [syng-im.components.invertible-scroll-view :refer [invertible-scroll-view]]
            
            ;; [syng-im.components.chat.message :refer [message]]
            ;; [syng-im.components.chat.new-message :refer [new-message NewMessage]]
            ))

;(defn generate-message [n]
;  {:id              n
;   :type            (if (= (rem n 4) 3)
;                      :audio
;                      :text)
;   :body            (if (= (rem n 3) 0)
;                      (apply str n "." (repeat 5 " This is a text."))
;                      (str n ". This is a text."))
;   :outgoing        (< (rand) 0.5)
;   :delivery-status (if (< (rand) 0.5) :delivered :seen)
;   :date            "TODAY"
;   :new-day         (= (rem n 3) 0)})
;
;(defn generate-messages [n]
;  (map generate-message (range 1 (inc n))))

;(defn load-messages []
;  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
;                                                  (not= row1 row2))})
;                   (vec (generate-messages 100))))
;

;;temp
;; (swap! state/app-state assoc :abc "xyz")

(defn to-datasource [msgs]
  (-> (data-source {:rowHasChanged (fn [row1 row2]
                                     (not= row1 row2))})
      (clone-with-rows msgs)))



(defn render-row [row section-id row-id]
  (list-item [message-view {} (js->clj row :keywordize-keys true)]))

(defn chat [{:keys [navigator]}]
  (let [greeting (subscribe [:get-greeting])
        chat-id 1
        ;; messages (subscribe [:get-messages chat-id])
        ]
    (fn []
      (let [messages-ds (when messages
                          (to-datasource messages))]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         [toolbar-android {:logo          res/logo-icon
                            :title         "Chat name"
                            :titleColor    "#4A5258"
                            :subtitle      "Last seen just now"
                            :subtitleColor "#AAB2B2"
                            :navIcon       res/nav-back-icon
                            :style         {:backgroundColor "white"
                                            :height          56
                                            :elevation       2}
                            :onIconClicked (fn []
                                             (nav/nav-pop nav))}]
         ;; temp commented
         ;; (when messages-ds
         ;;   [list-view {:dataSource messages-ds
         ;;               :renderScrollComponent
         ;;               (fn [props]
         ;;                 (r/as-element [invertible-scroll-view {:inverted true}]))
         ;;               :renderRow  render-row
         ;;               :style      {:backgroundColor "white"}}])
         ;; (new-message {:chat-id chat-id})
         ]))))
