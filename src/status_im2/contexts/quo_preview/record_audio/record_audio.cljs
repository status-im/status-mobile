(ns status-im2.contexts.quo-preview.record-audio.record-audio
  (:require [quo2.components.record-audio.record-audio.view :as record-audio]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.re-frame :as rf]
            [status-im2.common.alert.events :as alert]
            [utils.i18n :as i18n]
            [react-native.permissions :as permissions]
            [status-im2.constants :as constants]))

(defonce record-audio-permission-granted (reagent/atom false))

(defn preview-record-audio
  []
  (let [message                            (reagent/atom
                                            "Press & hold the mic button to start recording...")
        on-send                            #(reset! message (str "onSend event triggered. File path: "
                                                                 %))
        on-start-recording                 #(reset! message "onStartRecording event triggered.")
        on-reviewing-audio                 #(reset! message "onReviewingAudio event triggered.")
        on-cancel                          #(reset! message "onCancel event triggered.")
        on-check-audio-permissions         (fn []
                                             (permissions/permission-granted?
                                              :record-audio
                                              #(reset! record-audio-permission-granted %)
                                              #(reset! record-audio-permission-granted false)))
        on-request-record-audio-permission (fn []
                                             (rf/dispatch
                                              [:request-permissions
                                               {:permissions [:record-audio]
                                                :on-allowed
                                                #(reset! record-audio-permission-granted true)
                                                :on-denied
                                                #(js/setTimeout
                                                  (fn []
                                                    (alert/show-popup
                                                     (i18n/label :t/audio-recorder-error)
                                                     (i18n/label
                                                      :t/audio-recorder-permissions-error)))
                                                  50)}]))]
    (fn []
      [preview/preview-container
       [rn/view
        {:padding-top      150
         :align-items      :center
         :background-color :transparent
         :flex-direction   :row}
        [record-audio/record-audio
         {:record-audio-permission-granted    @record-audio-permission-granted
          :on-send                            on-send
          :on-start-recording                 on-start-recording
          :on-reviewing-audio                 on-reviewing-audio
          :on-cancel                          on-cancel
          :on-check-audio-permissions         on-check-audio-permissions
          :on-request-record-audio-permission on-request-record-audio-permission
          :max-duration-ms                    constants/audio-max-duration-ms}]]
       [quo/text {:style {:margin-horizontal 20}} @message]])))
