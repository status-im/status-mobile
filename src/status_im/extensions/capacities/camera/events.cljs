(ns status-im.extensions.capacities.camera.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.extensions.capacities.camera.views :as ext-views]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]))

;; Common

(handlers/register-handler-fx
 :extensions/camera-cancel
 (fn [_ [_ _ {{:keys [on-failure]} :data}]]
   (when on-failure
     (re-frame/dispatch (on-failure {:result "user cancelled"})))))

(handlers/register-handler-fx
 :extensions/camera-denied
 (fn [_ [_ {{:keys [on-failure]} :data}]]
   (when on-failure
     (re-frame/dispatch (on-failure {:result "user denied access to camera"})))))

;; Photo taker\picker

(handlers/register-handler-fx
 :extensions/camera-error
 (fn [cofx [_ error {:keys [on-failure]}]]
   (when on-failure
     (on-failure {:result error}))))

(handlers/register-handler-fx
 :extensions/camera-picture-taken
 (fn [cofx [_ data {{:keys [on-success]} :data back? :back?}]]
   (fx/merge cofx
             (on-success {:result data})
             (when back?
               (navigation/navigate-back)))))

(handlers/register-handler-fx
 :extensions/camera-picture
 (fn [_ [_ _ params]]
   (list-selection/show (ext-views/pick-or-take-picture-list-selection {:data params}))
   {}))

;; QR code scanner

(handlers/register-handler-fx
 :extensions/camera-qr-code-scanned
 (fn [cofx [_ _ qr-code {{:keys [on-success]} :data}]]
   (fx/merge cofx
             (on-success {:result qr-code})
             (navigation/navigate-back))))

(handlers/register-handler-fx
 :extensions/camera-qr-code
 (fn [{:keys [db] :as cofx} [_ _ {:keys [on-success on-failure]}]]
   (qr-scanner/scan-qr-code cofx {:deny-handler :extensions/camera-denied}
                            {:handler        :extensions/camera-qr-code-scanned
                             :cancel-handler :extensions/camera-cancel
                             :data           {:on-success on-success
                                              :on-failure on-failure}})))
