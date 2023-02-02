(ns status-im2.contexts.quo-preview.record-audio.record-audio
  (:require [quo2.components.record-audio.record-audio.view :as record-audio]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn cool-preview
  []
  (let [message            (reagent/atom "Press & hold the mic button to start recording...")
        on-send            #(reset! message (str "onSend event triggered. File path: " %))
        on-start-recording #(reset! message "onStartRecording event triggered.")
        on-reviewing-audio #(reset! message "onReviewingAudio event triggered.")
        on-cancel          #(reset! message "onCancel event triggered.")]
    (fn []
      [rn/view
       [rn/view
        {:padding-top      150
         :align-items      :center
         :background-color :transparent
         :flex-direction   :row}
        [record-audio/record-audio
         {:on-send            on-send
          :on-start-recording on-start-recording
          :on-reviewing-audio on-reviewing-audio
          :on-cancel          on-cancel}]]
       [quo/text {:style {:margin-horizontal 20}} @message]])))

(defn preview-record-audio
  []
  [rn/view {:flex 1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :never
     :scroll-enabled               false
     :header                       [cool-preview]
     :key-fn                       str}]])
