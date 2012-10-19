main_color="#f0810a"; //Main orange
header_bg="#7c7c7c";

bg_color="#ffffff";
bg2_color=util.color(bg_color).lume(0.3);
body_image='bg-body-domui.png';

//-- Main page title gradient
bg_ttl_gradient = "bg-ttl-domui.png";
bg_ttl_end = '#7c7c7c';
bg_ttl_text = '#ffffff';

button_bg="#6ba1eb";
button_fg="#ffffff";
button_img= "defaultButton.png";
button_height=24;

link_font_size="12px";
link_color="#2200cc";	// dark blue

highlight_bg="#ffbb43";	// row select: hard orange
highlight2_bg = util.color(highlight_bg).lighter(0.5);

//-- Header colors
data_tbl_header_bg="#7c7c7c";
data_tbl_header_text_color="#ffffff";
data_tbl_header_bg_img=""; //-- Table header doesn't have background image
data_tbl_header_btm_border=undefined;

//-- Selects and highlights ---
data_tbl_selected_bg="#bed6f8";	// blue
data_tbl_even_row_bg="#e8e8e8"; //greyish
data_tbl_cell_highlight_bg="#67267f";	// cell select: purple
data_tbl_cell_highlight_link_color_bg="#ffffff";	// link color of cell select: white
data_tbl_font_size = "12px";
data_tbl_expanding_row_bg = util.color(main_color).lighter(0.7);

readonly_bg="transparent"; 
readonly_border="#EEEEEF"; // bit darker grey

error_bg="#ffaaaa";	// red/pink light
error_border="#ff0000"; // red
error_fg="#ff0000"; // red
error_input_bg="#ffe5e5"; // error bg for input component

warning_bg="#fffeee"; // light yellow
warning_border="yellow";
warning_fg="black";

info_bg="#a9c5f1";		// blueish light
info_fg="blue";
info_border="blue";

// row odd/even
dt_rowhdr_bg="#98bbf3";
dt_rowhdr_fg="#ffffff";

//-- input bevel: darker grey / lighter grey
bevel_up="#ABADB3";
bevel_down="#BFCDD9";

//-- hovel input bevel for a component: dark blue/light blue
bevel_hover_up="#5794BF";
bevel_hover_down="#D7E8F8";

//-- button color
button_bg="#6290E1";
button_text_color="#ffffff";   

//-- Caption
caption_separator="hr-caption.png";

//-- Error colors (error panel)
error_bg="#a9c5f1";

//-- selected items background
selected_bg="#ff9436";

//-- Tab panel
tab_pnl_inactive_col = "#ffffff";
tab_pnl_hover_inactive_col = "#ffffff";
tab_pnl_active_col = "#67267f";
error_fg = "red";
tab_pnl_sep_bg = "#7c7c7c";

//-- Caption header
caption_bg=main_color;

//-- Bulk upload
upl_runing=highlight_bg;
upl_loading=data_tbl_cell_highlight_bg;

//loading bar
io_blk_wait = "io-blk-wait.gif";