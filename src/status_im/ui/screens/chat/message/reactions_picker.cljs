(ns status-im.ui.screens.chat.message.reactions-picker
  (:require [cljs-bean.core :as bean]
            [status-im.ui.screens.chat.message.styles :as styles]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.constants :as constants]
            [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.react :as react]
            [quo.animated :as animated]
            [quo.components.safe-area :as safe-area]
            [quo.core :as quo]))

(def tabbar-height 36)
(def text-input-height 54)

(def animation-duration 150)

(def scale         0.8)
(def translate-x   27)
(def translate-y   -24)

(def id-icon
  {"edit"   :main-icons/edit
   "pin"    :main-icons/pin
   "unpin"  :main-icons/pin
   "copy"   :main-icons/copy
   "reply"  :main-icons/reply
   "delete" :main-icons/delete})

(defn picker [{:keys [outgoing actions own-reactions on-close send-emoji timeline]}]
  [rn/view {:style (styles/container-style {:outgoing outgoing :timeline timeline})}
   [rn/view {:style (styles/reactions-picker-row)}
    (doall
     (for [[id resource] constants/reactions
           :let          [active (own-reactions id)]]
       ^{:key id}
       [rn/touchable-opacity {:accessibility-label (str "pick-emoji-" id)
                              :on-press            #(send-emoji id)}
        [rn/view {:style (styles/reaction-button active)}
         [rn/image {:source resource
                    :style  {:height 32
                             :width  32}}]]]))]
   (when (seq actions)
     [rn/view {:style (styles/quick-actions-container)}
      (for [action actions
            :let   [{:keys [id label on-press]} (bean/bean action)]]
        ^{:key id}
        [rn/touchable-opacity {:on-press (fn []
                                           (on-close)
                                           (js/setTimeout on-press animation-duration))}
         [rn/view {:style (styles/quick-actions-row)}
          [quo/text {:color  (if (= id "delete") :negative :link)
                     :weight :medium} label]
          ;; fallback to warning icon if id to icon mapping is not defined
          [icons/icon (get id-icon id :main-icons/warning)
           {:color (if (= id "delete") :red :blue)}]]])])])

(def modal
  (reagent/adapt-react-class
   (fn [props]
     (let [{outgoing       :outgoing
            animation      :animation
            spring         :spring
            top            :top
            message-height :messageHeight
            display-photo  :displayPhoto
            on-close       :onClose
            actions        :actions
            send-emoji     :sendEmoji
            own-reactions  :ownReactions
            children       :children
            timeline       :timeline}
           (bean/bean props)
           {bottom-inset :bottom}  (safe-area/use-safe-area)
           {window-height :height} (rn/use-window-dimensions)

           {picker-height    :height
            on-picker-layout :on-layout} (rn/use-layout)

           full-height   (+ message-height picker-height top)
           max-height    (- window-height bottom-inset tabbar-height text-input-height)
           top-delta     (max 0 (- full-height max-height))
           translation-x (if (and outgoing (not timeline))
                           translate-x
                           (* -1 translate-x))]
       (reagent/as-element
        [:<>
         [rn/view {:style {:position :absolute
                           :flex     1
                           :top      0
                           :bottom   0
                           :left     0
                           :right    0}}
          [rn/touchable-without-feedback
           {:on-press on-close}
           [animated/view {:style {:flex             1
                                   :opacity          animation
                                   :background-color "rgba(0,0,0,0.5)"}}]]]
         [animated/view {:pointer-events :box-none
                         :style          {:top       (- top top-delta)
                                          :left      0
                                          :right     0
                                          :position  :absolute
                                          :opacity   animation
                                          :transform [{:translateY (animated/mix animation top-delta 0)}]}}
          (into [:<>] (react/get-children children))
          [animated/view {:on-layout      on-picker-layout
                          :pointer-events :box-none
                          :style          (merge (styles/picker-wrapper-style {:display-photo? display-photo
                                                                               :timeline       timeline
                                                                               :outgoing       (and outgoing (not timeline))})
                                                 {:opacity   animation
                                                  :transform [{:translateX (animated/mix spring translation-x 0)}
                                                              {:translateY (animated/mix spring translate-y 0)}
                                                              {:scale (animated/mix spring scale 1)}]})}
           [picker {:outgoing      (and outgoing (not timeline))
                    :timeline      timeline
                    :actions       actions
                    :on-close      on-close
                    :own-reactions (into #{} (js->clj own-reactions))
                    :send-emoji    send-emoji
                    :animation     animation}]]]])))))
