
  //var baseUrl = "http://localhost:8080/";
  var baseUrl = "/";

  function loadTopLevelProcesses() {
    $.get(baseUrl + "processes", function(data, status){
      console.log(data);

      $.each(data, function(index, value) {
        console.log(value);
        /*
         <li class="nav-item">
                    <a class="nav-link active" href="#">
                      <span data-feather="home"></span>
                      Dashboard <span class="sr-only">(current)</span>
                    </a>
                  </li>
        */
        $("#topLevelProcesses").append("<li><a id='"+value.id+"' href='#' >" + value.name + "</a></li>");
        $("#"+value.id).click(function() {
          selectProcess(value);
        });
      });

    });
  }

  function loadProcess(processId, callback) {
    $.get(baseUrl + "processes/" + processId, function(data, status){
      console.log(data);
      selectProcess(data, callback);
    });
  }

  function selectProcess(process, callback) {
       $("#processName").text(process.name);
       if (process.parentProcessId) {
         $("#parentProcess").text(process.parentProcessName ? process.parentProcessName : process.parentProcessId);
         $("#parentProcess").click(function() {
            loadProcess(process.parentProcessId);
         });
       } else {
         $("#parentProcess").text("");
       }
       $("#details").empty();
       loadDiagram(process, callback);
  }

  // viewer instance
  var bpmnViewer = new BpmnJS({
    container: '#bpmnjs'
  });


  function loadDiagram(process, callback) {

        var diagramUrl = baseUrl + "processes/" + process.id + "/xml";

        /**
         * Open diagram in our viewer instance.
         */
        async function openDiagram(bpmnXML) {
          // import diagram
          try {
            await bpmnViewer.importXML(bpmnXML);

            // add additional information as overlay
            var canvas = bpmnViewer.get('canvas');
            var overlays = bpmnViewer.get('overlays');

            // zoom to fit full viewport
            //canvas.zoom('fit-viewport');
            canvas.zoom(0.5, {x: 0, y: 0}) // {x: 167, y: 247.5}

            addOverlaysServiceTask(overlays, process);
            addOverlaysCallActivities(overlays, process);
            addOverlaysUserTasks(overlays, process);

            if (callback) {
              callback();
            }

          } catch (err) {
            console.error('could not import BPMN 2.0 diagram', err);
          }
        }

        // load external diagram file via AJAX and open it
        $.get(diagramUrl, openDiagram, 'text');
  }

  function addOverlaysServiceTask(overlays, process) {
     process.serviceTasks.forEach( task => {
        var overlayHtml = $('<div class="diagram-note"><a href="#" id="link_task_'+task.id+'">' + task.system + '</a></div>');
        overlays.add(task.id, 'SERVICE', {
              position: {
                bottom: 0,
                left: 0
              },
              html: overlayHtml
        });
        $("#link_task_"+task.id).click(function() {
          loadSystem(task.system);
        });
     });
   }

   function loadSystem(systemId) {
        $("#details").empty();

        $("#details").append("<p>System: <b>" + systemId + "</b></p>");
        $("#details").append("<p>Also used in: </p>");
        $("#details").append("<ul id='usedIn'></ul>");
        $.get(baseUrl + "systems/" + systemId + "/usage", function(data, status){
          console.log(data);
          $.each(data, function(index, serviceTask) {
            console.log(serviceTask);
            var element = $("#usedIn").append("<li><a id='link_system_process_"+serviceTask.processId+"' href='#'>" + serviceTask.processName + " -> " + serviceTask.name + "</a></li>");
            $("#link_system_process_"+serviceTask.processId).click(function() {
              loadProcess(serviceTask.processId, function() {
                bpmnViewer.get('canvas').addMarker(serviceTask.id, 'highlight');
                loadSystem(serviceTask.system);
              });
            });
          });
        });
   }

   function addOverlaysCallActivities(overlays, process) {
       process.callActivities.forEach( task => {
           var overlayHtml = $('<div class="diagram-note"><ul></ul></div>');
           if (task.calledProcesses) {
             for (const calledProcess of task.calledProcesses) {
                  overlayHtml.append("<li><a id='link_process_"+calledProcess.id+"' href='#'>" + calledProcess.variant + ": " + calledProcess.name + "</a></li>");
             }
           }
           overlays.add(task.id, 'CALL', {
                  position: {
                    bottom: 0,
                    left: 0
                  },
                  html: overlayHtml
           });
           // Need to do this after the element has really been added to HTML
           if (task.calledProcesses) {
             for (const calledProcess of task.calledProcesses) {
                  $("#link_process_"+calledProcess.id).click(function() {
                    loadProcess(calledProcess.id);
                  });
             }
           }

       });
  }

  function addOverlaysUserTasks(overlays, process) {
     process.userTasks.forEach( task => {
        var overlayHtml = $('<div class="diagram-note"><a href="#" id="link_task_'+task.id+'">' + task.assignment + '</a></div>');
        overlays.add(task.id, 'SERVICE', {
              position: {
                bottom: 0,
                left: 0
              },
              html: overlayHtml
        });
        $("#link_task_"+task.id).click(function() {
          loadAssignment(task.assignment);
        });
     });
   }

   function loadAssignment(assignment) {
        $("#details").empty();

        $("#details").append("<p>User/group: <b>" + assignment + "</b></p>");
        $("#details").append("<p>Also doing: </p>");
        $("#details").append("<ul id='usedIn'></ul>");
        $.get(baseUrl + "users/" + assignment + "/usage", function(data, status){
          console.log(data);
          $.each(data, function(index, value) {
            console.log(value);
            var element = $("#usedIn").append("<li><a id='link_assignment_process_"+value.processId+"' href='#'>" + value.processName + " -> " + value.name + "</a></li>");
            element.click(function() {
              loadProcess(value.processId, function() {
                bpmnViewer.get('canvas').addMarker(value.id, 'highlight');
              });
            });
          });
        });
   }

  loadTopLevelProcesses();