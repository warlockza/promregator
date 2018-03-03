# Enrichment performed by Promregator

Promregator automatically enriches the metrics provided by the targets with additional labels.
Moreover, it also provides additional metrics to support you.

In general, there are three cases where additional metrics/labels may appear:

* Additional labels for provided metrics by the targets.
* Additional metrics measuring the communication to the targets.
* Additional metrics measuring Promregator itself.


## Additional Labels for Provided Metrics by the Targets
The following labels are automatically added to metrics, which are received from targets:

* The name of the Cloud Foundry organization in which the application is running (`org_name`)
* The name of the Cloud Foundry space in which the application is running (`space_name`)
* The name of the Cloud Foundry application which is running (`app_name`)
* The instance identifier (GUID of the application plus its instance number separated by a colon) from where the data was fetched (`instance`).

For you this means that assuming that a target, 

* which is running in CF org `cforg`, 
* CF space `cfspace` 
* as application called `cfapp`,
* having GUID `707d58b2-00ce-435a-845c-79eff28afe8c`
* and running as the first instance

exposing the following two metric samples
```
metric_without_label 1.0
metric_with_label{mylabel="myvalue"} 2.0
```
Promregator returns the following metrics (amongst other) to the caller:
```
metric_without_label{org_name="cforg",space_name="cfspace",app_name="cfapp",instance="707d58b2-00ce-435a-845c-79eff28afe8c:0"} 1.0
metric_with_label{mylabel="myvalue",org_name="cforg",space_name="cfspace",app_name="cfapp",instance="707d58b2-00ce-435a-845c-79eff28afe8c:0"} 2.0
```
By this, you may aggregate the metrics data in your Prometheus server according to your needs, allowing any drilldown you wish, even to the lowest level of a single instance.

Note that there is no label `promregator` added to these metrics.

## Additional Metrics Measuring the Communication to the Targets

Promregator also monitors the connectivity to the targets. The result of the monitoring also made available to
the caller via Prometheus metrics. For this, the following metrics are exposed:

* `promregator_request_latency`: a [Prometheus histogram](https://prometheus.io/docs/practices/histograms/), 
  which returns the latency which was necessary to retrieve the metrics from the target.
* `promregator_up`: a [Prometheus Gauge](https://prometheus.io/docs/concepts/metric_types/) which indicates whether an instance was reachable or not (similar to the [gauge provided for Prometheus' own monitoring](https://prometheus.io/docs/concepts/jobs_instances/)).

Note that additionally to the labels `org_name`, `space_name`, `app_name` and `instance` (which tells you the target, which the metric is referring to), the label `promregator` is set to `true` indicating that the value of the metric sample was created by Promregator itself and is not originated by any of the targets.

Here is an example how such metric samples may look like:
```
# HELP promregator_up Indicator, whether the target of promregator is available
# TYPE promregator_up gauge
promregator_up{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",promregator="true",} 1.0
promregator_up{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",promregator="true",} 0.0

# HELP promregator_request_latency The latency, which the targets of the promregator produce
# TYPE promregator_request_latency histogram
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.005",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.01",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.025",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.05",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.075",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.1",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.25",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.5",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="0.75",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="1.0",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="2.5",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="5.0",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="7.5",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="10.0",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",le="+Inf",promregator="true",} 1.0
promregator_request_latency_count{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",promregator="true",} 1.0
promregator_request_latency_sum{org_name="cforg",space_name="dev",app_name="testapp",instance="262ec022-8366-4c49-ac13-f50b35a78154:0",promregator="true",} 0.21851916
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.005",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.01",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.025",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.05",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.075",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.1",promregator="true",} 0.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.25",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.5",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="0.75",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="1.0",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="2.5",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="5.0",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="7.5",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="10.0",promregator="true",} 1.0
promregator_request_latency_bucket{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",le="+Inf",promregator="true",} 1.0
promregator_request_latency_count{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",promregator="true",} 1.0
promregator_request_latency_sum{org_name="cforg",space_name="dev",app_name="testapp2",instance="9897cda1-2673-4d75-adf2-3132eea90873:0",promregator="true",} 0.218502344
```


## Additional Metrics Measuring Promregator Itself




