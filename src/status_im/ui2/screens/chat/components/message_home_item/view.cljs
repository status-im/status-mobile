(ns status-im.ui2.screens.chat.components.message-home-item.view
  (:require [clojure.string :as string]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.utils.datetime :as time]
            [quo2.foundations.typography :as typography]
            [quo2.components.notifications.info-count :refer [info-count]]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo.react-native :as rn]
            [status-im.ui.screens.chat.sheets :as sheets]
            [quo.platform :as platform]
<<<<<<< HEAD
<<<<<<< HEAD
            [quo2.components.markdown.text :as text]
=======
>>>>>>> 5fcc08fd3... refactor
=======
            [quo2.components.markdown.text :as text]
>>>>>>> 8e74397a9... lint
            [status-im.ui2.screens.chat.components.message-home-item.style :as style]))

(def max-subheader-length 50)

(defn truncate-literal [literal]
  (when literal
    (let [size (min max-subheader-length (.-length literal))]
      {:components (.substring literal 0 size)
       :length     size})))

(defn add-parsed-to-subheader [acc {:keys [type destination literal children]}]
  (let [result (case type
                 "paragraph"
                 (reduce
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 8e74397a9... lint
=======
>>>>>>> 29504b409... lint
                  (fn [{:keys [_ length] :as acc-paragraph} parsed-child]
                    (if (>= length max-subheader-length)
                      (reduced acc-paragraph)
                      (add-parsed-to-subheader acc-paragraph parsed-child)))
                  {:components [rn/text]
                   :length     0}
                  children)
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> 1e719b3ab... icons fix
                   (fn [{:keys [_ length] :as acc-paragraph} parsed-child]
                     (if (>= length max-subheader-length)
                       (reduced acc-paragraph)
                       (add-parsed-to-subheader acc-paragraph parsed-child)))
                   {:components [rn/text]
                    :length     0}
                   children)
<<<<<<< HEAD
>>>>>>> 5fcc08fd3... refactor
=======
>>>>>>> 8e74397a9... lint
=======
>>>>>>> 1e719b3ab... icons fix
=======
>>>>>>> 29504b409... lint

                 "mention"
                 {:components [rn/text (<sub [:contacts/contact-name-by-identity literal])]
                  :length     4} ;; we can't predict name length so take the smallest possible

                 "status-tag"
                 (truncate-literal (str "#" literal))

                 "link"
                 (truncate-literal destination)

                 (truncate-literal literal))]
    {:components (conj (:components acc) (:components result))
     :length     (+ (:length acc) (:length result))}))

(defn render-subheader
  "Render the preview of a last message to a maximum of max-subheader-length characters"
  [parsed-text]
  (let [result
        (reduce
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 8e74397a9... lint
=======
>>>>>>> 29504b409... lint
         (fn [{:keys [_ length] :as acc-text} new-text-chunk]
           (if (>= length max-subheader-length)
             (reduced acc-text)
             (add-parsed-to-subheader acc-text new-text-chunk)))
         {:components [rn/text {:style               (merge typography/paragraph-2 typography/font-regular
                                                            {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                                             :width "90%"})
                                :number-of-lines     1
                                :ellipsize-mode      :tail
                                :accessibility-label :chat-message-text}]
          :length     0}
         parsed-text)]
<<<<<<< HEAD
<<<<<<< HEAD
    (:components result)))

(defn display-name-view [display-name contact timestamp]
  [rn/view {:style {:flex-direction :row}}
   [text/text {:weight :semi-bold
               :size   :paragraph-1}
    display-name]
   (if (:ens-verified contact)
     [rn/view {:style {:margin-left 5 :margin-top 4}}
      [icons/icon :main-icons2/verified {:no-color true
                                         :size 12
                                         :color (colors/theme-colors colors/success-50 colors/success-60)}]]
     (when (:added? contact)
       [rn/view {:style {:margin-left 5 :margin-top 4}}
        [icons/icon :main-icons2/contact {:no-color true
                                          :size 12
                                          :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]))
   [text/text {:style (style/timestamp)}
    (time/to-short-str timestamp)]])

(defn messages-home-item [item]
  (let [{:keys [chat-id color group-chat last-message timestamp name unviewed-mentions-count unviewed-messages-count]} item
=======
=======
>>>>>>> 1e719b3ab... icons fix
          (fn [{:keys [_ length] :as acc-text} new-text-chunk]
            (if (>= length max-subheader-length)
              (reduced acc-text)
              (add-parsed-to-subheader acc-text new-text-chunk)))
          {:components [rn/text {:style               (merge typography/paragraph-2 typography/font-regular
                                                             {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                                              :width "90%"})
                                 :number-of-lines     1
                                 :ellipsize-mode      :tail
                                 :accessibility-label :chat-message-text}]
           :length     0}
          parsed-text)]
<<<<<<< HEAD
=======
>>>>>>> 8e74397a9... lint
=======
>>>>>>> 1e719b3ab... icons fix
=======
>>>>>>> 29504b409... lint
    (:components result)))

(defn display-name-view [display-name contact timestamp]
  [rn/view {:style {:flex-direction :row}}
   [text/text {:weight :semi-bold
               :size   :paragraph-1}
    display-name]
   (if (:ens-verified contact)
     [rn/view {:style {:margin-left 5 :margin-top 4}}
      [icons/icon :main-icons2/verified {:no-color true
                                         :size 12
                                         :color (colors/theme-colors colors/success-50 colors/success-60)}]]
     (when (:added? contact)
       [rn/view {:style {:margin-left 5 :margin-top 4}}
        [icons/icon :main-icons2/contact {:no-color true
                                          :size 12
                                          :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]))
   [text/text {:style (style/timestamp)}
    (time/to-short-str timestamp)]])

(defn messages-home-item [item]
  (let [{:keys [chat-id color group-chat last-message timestamp name]} item
>>>>>>> 5fcc08fd3... refactor
        display-name (if-not group-chat (first (<sub [:contacts/contact-two-names-by-identity chat-id])) name)
        contact      (when-not group-chat (<sub [:contacts/contact-by-address chat-id]))
        photo-path   (when-not (empty? (:images contact)) (<sub [:chats/photo-path chat-id]))]
    [rn/touchable-opacity (merge {:style         (style/container)
                                  :on-press      (fn []
                                                   (>evt [:dismiss-keyboard])
                                                   (if platform/android?
                                                     (>evt [:chat.ui/navigate-to-chat-nav2 chat-id])
                                                     (>evt [:chat.ui/navigate-to-chat chat-id]))
                                                   (>evt [:search/home-filter-changed nil])
                                                   (>evt [:accept-all-activity-center-notifications-from-chat chat-id]))
                                  :on-long-press #(>evt [:bottom-sheet/show-sheet
                                                         {:content (fn [] [sheets/actions item])}])})
     (if group-chat
       [rn/view {:style (style/group-chat-icon color)}
        [icons/icon :main-icons2/group {:size 16 :color colors/white-opa-70}]]
       [user-avatar/user-avatar {:full-name         display-name
                                 :profile-picture   photo-path
                                 :status-indicator? true
                                 :online?           true
                                 :size              :small
                                 :ring?             false}])

     [rn/view {:style {:margin-left 8}}
<<<<<<< HEAD
<<<<<<< HEAD
      [display-name-view display-name contact timestamp]
      (if (string/blank? (get-in last-message [:content :parsed-text]))
        [text/text {:size  :paragraph-2
                    :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
         (get-in last-message [:content :text])]
        [render-subheader (get-in last-message [:content :parsed-text])])]
     (if (> unviewed-mentions-count 0)
       [info-count unviewed-mentions-count {:top 16}]
       (when (> unviewed-messages-count 0)
=======
      [rn/view {:style {:flex-direction :row}}
       [rn/text {:style (merge typography/paragraph-1 typography/font-semi-bold
                               {:color (colors/theme-colors colors/neutral-100 colors/white)})}
        display-name]
       (if (:ens-verified contact)
         [rn/view {:style {:margin-left 5 :margin-top 4}}
          [icons/icon :main-icons2/verified {:size 12 :color (colors/theme-colors colors/success-50 colors/success-60)}]]
         (when (:added? contact)
           [rn/view {:style {:margin-left 5 :margin-top 4}}
            [icons/icon :main-icons2/contact {:size 12 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]))
       [rn/text {:style (merge typography/font-regular typography/label
                               {:color       (colors/theme-colors colors/neutral-50 colors/neutral-40)
                                :margin-top  3
                                :margin-left 8})}
        (time/to-short-str timestamp)]] ; placeholder for community chats to avoid crashing until implemented
=======
      [display-name-view display-name contact timestamp]
>>>>>>> 8e74397a9... lint
      (if (string/blank? (get-in last-message [:content :parsed-text]))
        [text/text {:size  :paragraph-2
                    :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
         (get-in last-message [:content :text])]
        [render-subheader (get-in last-message [:content :parsed-text])])]
     (if (> (:unviewed-mentions-count item) 0)
       [info-count (:unviewed-mentions-count item) {:top 16}]
       (when (> (:unviewed-messages-count item) 0)
>>>>>>> 5fcc08fd3... refactor
         [rn/view {:style (style/count-container)}]))]))


