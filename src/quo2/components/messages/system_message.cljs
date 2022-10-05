(ns quo2.components.messages.system-message
  (:require [status-im.i18n.i18n :as i18n]
            [quo.react-native :as rn]
            [status-im.utils.core :as utils]
            [quo.theme :as theme]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.reanimated :as ra]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.components.avatars.icon-avatar :as icon-avatar]))

(def themes-landed {:pinned  colors/primary-50-opa-5
                    :added   colors/primary-50-opa-5
                    :deleted colors/danger-50-opa-5})

(def themes
  {:light {:text colors/neutral-100
           :time colors/neutral-50
           :bg   {:default colors/white
                  :pressed colors/neutral-5
                  :landed  themes-landed}}
   :dark  {:text colors/white
           :time colors/neutral-40
           :bg   {:default colors/neutral-90
                  :pressed colors/neutral-80
                  :landed  themes-landed}}})

(defn get-color [& keys]
  (reduce (fn [acc k] (get acc k (reduced acc)))
          ((theme/get-theme) themes) (vec keys)))

(defn sm-timestamp [timestamp-str]
  [rn/view {:margin-left 6}
   [text/text {:size  :label
               :style {:color          (get-color :time)
                       :text-transform :none}}
    timestamp-str]])

(defn sm-icon [{:keys [icon color opacity]}]
  [rn/view {:align-items  :center
            :margin-right 8}
   [icon-avatar/icon-avatar {:size    :medium
                             :icon    icon
                             :color   color
                             :opacity opacity}]])

(defn sm-user-avatar [image]
  [rn/view {:margin-right 4}
   [user-avatar/user-avatar {:status-indicator? false
                             :online?           false
                             :size              :xxxs
                             :profile-picture   image
                             :ring?             false}]])

(defmulti sm-render :type)

(defmethod sm-render :deleted [{:keys [state action timestamp-str]}]
  [rn/view {:align-items     :center
            :justify-content :space-between
            :flex            1
            :flex-direction  :row}
   [rn/view {:align-items    :center
             :flex-direction :row}
    [sm-icon {:icon    :main-icons/delete16
              :color   :danger
              :opacity (if (= state :landed) 0 5)}]
    [text/text {:size  :paragraph-2
                :style {:color        (get-color :text)
                        :margin-right 5}}
     (i18n/label (if action :message-deleted-for-you :message-deleted))]
    (when (nil? action) [sm-timestamp timestamp-str])]
   (when action [button/button {:size   24
                                :before :main-icons/timeout
                                :type   :grey} (i18n/label :undo)])])

(defmethod sm-render :added [{:keys [state mentions timestamp-str]}]
  [rn/view {:align-items    :center
            :flex-direction :row}
   [sm-icon {:icon    :main-icons/add-user16
             :color   :primary
             :opacity (if (= state :landed) 0 5)}]
   [sm-user-avatar (:image (first mentions))]
   [text/text {:weight :semi-bold
               :size   :paragraph-2}
    (:name (first mentions))]
   [text/text {:size  :paragraph-2
               :style {:color        (get-color :text)
                       :margin-left  3
                       :margin-right 3}}
    (i18n/label :added)]
   [sm-user-avatar (:image (second mentions))]
   [text/text {:weight :semi-bold
               :size   :paragraph-2}
    (:name (second mentions))]
   [sm-timestamp timestamp-str]])

(defmethod sm-render :pinned [{:keys [state pinned-by content timestamp-str]}]
  [rn/view {:flex-direction :row
            :flex           1
            :align-items    :center}
   [sm-icon {:icon    :main-icons/pin16
             :color   :primary
             :opacity (if (= state :landed) 0 5)}]
   [rn/view {:flex-direction :column
             :flex           1}
    [rn/view {:align-items    :baseline
              :flex-direction :row}
     [text/text {:size   :paragraph-2
                 :weight :semi-bold
                 :style  {:color (get-color :text)}}
      (utils/truncate-str pinned-by 18)]
     [rn/view {:margin-left  4
               :margin-right 2}
      [text/text {:size  :paragraph-2
                  :style {:color (get-color :text)}}
       (i18n/label :pinned-a-message)]]
     [sm-timestamp timestamp-str]]
    [rn/view {:flex-direction :row}
     [rn/view {:flex-direction :row
               :margin-right   4}
      [sm-user-avatar (:image (:mentions content))]
      [text/text {:weight :semi-bold
                  :size   :label}
       (:name (:mentions content))]]
     (when (seq (:text content))
       [rn/view {:margin-right   20
                 :flex-direction :row
                 :flex           1}
        [text/text {:size            :label
                    :style           {:color (get-color :text)}
                    :number-of-lines 1
                    :ellipsize-mode  :tail}
         (:text content)]])
     [rn/view {:justify-content :flex-end
               :flex-direction  :row
               :min-width       10}
      (when (seq (:info content))
        [text/text {:size  :label
                    :style {:color (get-color :time)}}
         (utils/truncate-str (:info content) 24)])]]]])

(defn system-message [{:keys [type] :as message}]
  [:f>
   (fn []
     (let [sv-color (ra/use-shared-value (get-color :bg :landed type))]
       (ra/animate-shared-value-with-delay
        sv-color (get-color :bg :default type) 0 :linear 1000)
       [ra/touchable-opacity
        {:on-press #(ra/set-shared-value
                     sv-color (get-color :bg :pressed type))
         :style    (ra/apply-animations-to-style
                    {:background-color sv-color}
                    {:flex-direction     :row
                     :flex               1
                     :border-radius      16
                     :padding-vertical   9
                     :padding-horizontal 11
                     :width              359
                     :height             52
                     :background-color   sv-color})}
        [sm-render message]]))])
