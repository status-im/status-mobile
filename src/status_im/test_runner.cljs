(ns status-im.test-runner
  {:dev/always true}
  (:require [cljs.test :as ct]
            [clojure.string :as string]
            [shadow.test :as st]
            [shadow.test.env :as env]
            [utils.re-frame :as rf]
            [i18n.i18n :as i18n]
            [status-im2.setup.i18n-resources :as i18n-resources]))

(i18n/init (name i18n-resources/default-device-language) (clj->js i18n-resources/translations-by-locale))

(defonce repl? (atom false))

(defmethod ct/report [::ct/default :end-run-tests]
  [m]
  (when-not @repl?
    (if (ct/successful? m)
      (js/process.exit 0)
      (js/process.exit 1))))

;; get-test-data is a macro so this namespace REQUIRES :dev/always hint ns so that it is always
;; recompiled
(defn ^:dev/after-load reset-test-data!
  []
  (-> (env/get-test-data)
      (env/reset-test-data!)))

(defn parse-args
  [args]
  (reduce
   (fn [opts arg]
     (cond
       (= "--help" arg)
       (assoc opts :help true)

       (= "--list" arg)
       (assoc opts :list true)

       (= "--repl" arg)
       (assoc opts :repl true)

       (string/starts-with? arg "--test=")
       (let [test-arg  (subs arg 7)
             test-syms
             (->> (string/split test-arg ",")
                  (map symbol))]
         (update opts :test-syms into test-syms))

       :else
       (do (println (str "Unknown arg: " arg))
           opts)))
   {:test-syms []}
   args))

(defn find-matching-test-vars
  [test-syms]
  ;; FIXME: should have some kind of wildcard support
  (let [test-namespaces
        (->> test-syms
             (filter simple-symbol?)
             (set))
        test-var-syms
        (->> test-syms
             (filter qualified-symbol?)
             (set))]

    (->> (env/get-test-vars)
         (filter (fn [the-var]
                   (let [{:keys [name ns]} (meta the-var)]
                     (or (contains? test-namespaces ns)
                         (contains? test-var-syms (symbol ns name)))))))))

(defn execute-cli
  [{:keys [test-syms help list repl] :as _opts}]
  (let [test-env
        (-> (ct/empty-env)
            ;; can't think of a proper way to let CLI specify custom reporter?
            ;; :report-fn is mostly for UI purposes, CLI should be fine with default report
            #_(assoc :report-fn
                     (fn [m]
                       (tap> [:test m (ct/get-current-env)])
                       (prn m))))]

    (cond
      help
      (do
        (println "Usage:")
        (println "  --list (list known test names)")
        (println
         "  --test=<ns-to-test>,<fqn-symbol-to-test> (run test for namespace or single var, separated by comma)")
        (println "  --repl (start node without automatically running tests)"))

      list
      (doseq [[ns ns-info]
              (->> (env/get-tests)
                   (sort-by first))]
        (println "Namespace:" ns)
        (doseq [var  (:vars ns-info)
                :let [m (meta var)]]
          (println (str "  " (:ns m) "/" (:name m))))
        (println "---------------------------------"))

      repl
      (do
        (reset! repl? true)
        (js/process.on "SIGINT" #(js/process.exit 0)))

      (seq test-syms)
      (let [test-vars (find-matching-test-vars test-syms)]
        (st/run-test-vars test-env test-vars))

      :else
      (st/run-all-tests test-env nil))))

(defn ^:export main
  [& args]
  (reset-test-data!)
  (rf/set-mergeable-keys #{:filters/load-filters
                           :pairing/set-installation-metadata
                           :dispatch-n
                           :status-im.ens.core/verify-names
                           :shh/send-direct-message
                           :shh/remove-filter
                           :transport/confirm-messages-processed
                           :group-chats/extract-membership-signature
                           :utils/dispatch-later
                           :json-rpc/call})

  (let [opts (parse-args args)]
    (execute-cli opts)))
