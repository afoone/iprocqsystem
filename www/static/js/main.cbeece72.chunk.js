(window.webpackJsonp=window.webpackJsonp||[]).push([[0],{11:function(e,t,n){},18:function(e,t,n){e.exports=n(50)},24:function(e,t,n){},25:function(e,t,n){},50:function(e,t,n){"use strict";n.r(t);var a=n(17),o=n.n(a),r=n(0),s=n.n(r),l=n(2),i=n(3),c=n(5),u=n(4),m=n(6),d=(n(11),function(e){var t=e.rowStyle,n=e.textStyle,a=e.message;return s.a.createElement("div",{style:t,className:"marquee"},s.a.createElement("div",{style:n},s.a.createElement("span",null,a),s.a.createElement("span",null,a)))}),h=function(e){function t(){return Object(l.a)(this,t),Object(c.a)(this,Object(u.a)(t).apply(this,arguments))}return Object(m.a)(t,e),Object(i.a)(t,[{key:"render",value:function(){var e=this.props,t=(e.rowHeight,e.bgColor),n=e.textStyle,a=e.message,o={backgroundColor:t,paddingTop:"1em"},r={backgroundColor:t,height:"100%",paddingTop:"2em"};return s.a.createElement("div",{className:"ui grid"},s.a.createElement("div",{className:"row"},s.a.createElement("div",{className:"four wide column",style:o},s.a.createElement("img",{src:"/ivologo.png"})),s.a.createElement("div",{className:"ten wide column",style:o},s.a.createElement(d,{rowStyle:r,textStyle:n,message:a})),s.a.createElement("div",{className:"center aligned two wide column",style:o},s.a.createElement("iframe",{src:"http://free.timeanddate.com/clock/i6pd5a52/n4529/szw110/szh110/hocfff/hbw0/cf100/hgr0/fas20/facfff/fdi86/mqc000/mqs2/mql3/mqw4/mqd70/mhc000/mhs2/mhl3/mhw4/mhd70/mmv0/hhs3/hms3/hsc00f",frameBorder:"0",width:"110",height:"110"}))))}}]),t}(s.a.Component),g=(s.a.Component,n(24),function(e){function t(){return Object(l.a)(this,t),Object(c.a)(this,Object(u.a)(t).apply(this,arguments))}return Object(m.a)(t,e),Object(i.a)(t,[{key:"render",value:function(){var e=this.props,t=e.textStyle,n=e.rowHeight,a=e.bgColor,o=e.element,r=e.header;if(!o&&!r)return null;var l=r;r||(l=[o.point.includes("#")?o.point.substr(0,o.point.indexOf("#")):"-",o.customerPrefix+o.customerNumber,o.point.includes("#")?o.point.substr(o.point.indexOf("#")+1,100):o.point]);var i={height:n+"%",border:0,backgroundColor:a,paddingTop:"3rem"};return s.a.createElement("div",{className:"ui segment",style:i},s.a.createElement("div",{className:"ui three column grid",style:t},l.map(function(e,t){return s.a.createElement("div",{key:t,className:"column board-text "+(o&&"STATE_INVITED"===o.state?"blink_me":"")},e)})))}}]),t}(s.a.Component)),p=function(e){return s.a.createElement("div",{className:"ui inverted divider ",style:e.style})},f=(n(25),n(7)),b=n.n(f),v=n(8),y=n.n(v),S=function(e){function t(e){var n;return Object(l.a)(this,t),(n=Object(c.a)(this,Object(u.a)(t).call(this,e))).handleSongFinishedPlaying=function(){n.setState({playStatus:b.a.status.STOPPED})},n.state={playStatus:b.a.status.STOPPED,header:["SERVICIO","TURNO","CONSULTA"],encolados:[],message:"Bienvenidos"},n}return Object(m.a)(t,e),Object(i.a)(t,[{key:"componentDidMount",value:function(){var e=this;this.timer=setInterval(function(){return e.getItems()},200),this.timer=setInterval(function(){return e.checkSound()},600),this.timer=setInterval(function(){return e.getConfig()},6e3)}},{key:"componentWillUnmount",value:function(){this.timer=null}},{key:"getItems",value:function(){var e=this;y.a.get("http://localhost:8081/qsmartboard/records.jsp").then(function(t){console.log(t),e.setState({encolados:t.data})})}},{key:"getConfig",value:function(){var e=this;y.a.get("http://localhost:8000/boardconfig/1/").then(function(t){console.log("CONFIGURING"),console.log(t.data),e.setState({message:t.data.welcome_message})})}},{key:"checkSound",value:function(){var e=this;y.a.get("http://localhost:8081/qsmartboard/calling.jsp").then(function(t){console.log("calling invited"),console.log(t.data.invited),"true"===t.data.invited&&e.setState({playStatus:b.a.status.PLAYING})})}},{key:"render",value:function(){var e=this,t=85/this.props.rowNumber;console.log(t);var n={backgroundColor:this.props.bgColor},a={color:"cyan",fontSize:"7rem"};return s.a.createElement("div",{className:"fullHeight",style:n},s.a.createElement(b.a,{url:"/ding.wav",playStatus:this.state.playStatus,onFinishedPlaying:this.handleSongFinishedPlaying}),s.a.createElement(h,{rowHeight:10,bgColor:this.props.bgColor,textStyle:{color:"lightyellow",fontSize:"4rem"},message:this.state.message}),s.a.createElement(p,{style:{color:"white"}}),s.a.createElement(g,{header:this.state.header,rowHeight:t,bgColor:this.props.bgColor,textStyle:{color:"whitesmoke",fontSize:"6rem"}}),s.a.createElement(p,null),this.state.encolados.map(function(n){return s.a.createElement(g,{key:n.addressRS,element:n,rowHeight:t,bgColor:e.props.bgColor,textStyle:a})}))}}]),t}(s.a.Component),w=function(e){function t(){return Object(l.a)(this,t),Object(c.a)(this,Object(u.a)(t).apply(this,arguments))}return Object(m.a)(t,e),Object(i.a)(t,[{key:"render",value:function(){return s.a.createElement(S,{rowNumber:6,bgColor:"#004e59"})}}]),t}(s.a.Component);o.a.render(s.a.createElement(w,null),document.querySelector("#root"))}},[[18,1,2]]]);
//# sourceMappingURL=main.cbeece72.chunk.js.map